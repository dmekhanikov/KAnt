package jetbrains.kant.dsl

import org.apache.tools.ant.*
import org.apache.tools.ant.Target
import org.apache.tools.ant.taskdefs.Definer.OnError
import java.io.File
import java.util.ArrayList
import java.util.HashMap
import java.lang.reflect.Field
import jetbrains.kant.common.valuesToMap
import jetbrains.kant.common.DefinitionKind
import kotlin.reflect.KProperty

private val DSL_TARGET = DSLTarget::class.java.name

abstract class DSLElement(val projectAO: Project, val targetAO: Target)

open class DSLTask(projectAO: Project, targetAO: Target,
                   val parentWrapperAO: RuntimeConfigurable?, // if it is null then it can be executed
                   val elementTag: String,
                   nearestExecutable: DSLTask?) : DSLElement(projectAO, targetAO) {
    val taskAO: UnknownElement = UnknownElement(elementTag)
    val wrapperAO: RuntimeConfigurable
    val attributes = HashMap<String, Any?>()
    private val taskContainers: ArrayList<Pair<DSLTaskContainer, DSLTaskContainer.() -> Unit>>?
    val nearestExecutable: DSLTask
    var nestedText: String? = null

    init {
        taskAO.qName = elementTag
        taskAO.taskType = ProjectHelper.genComponentName("", elementTag)
        taskAO.taskName = elementTag
        taskAO.project = projectAO
        taskAO.owningTarget = targetAO
        wrapperAO = RuntimeConfigurable(taskAO, taskAO.taskName)

        if (parentWrapperAO == null) {
            this.nearestExecutable = this
            taskContainers = ArrayList()
        } else {
            this.nearestExecutable = nearestExecutable!!
            taskContainers = null
        }
    }

    protected fun setAttributes() {
        for (attr in attributes) {
            if (attr.key == "id") {
                projectAO.addIdReference(attr.value as String, taskAO)
            }
            val storedValue = attr.value
            val value: Any? =
                    if (storedValue is DSLReference<*>) {
                        storedValue.refid
                    } else {
                        storedValue
                    }
            wrapperAO.setAttribute(attr.key, value)
        }
        if (nestedText != null) {
            wrapperAO.addText(nestedText)
        }
    }

    fun defineComponent(name: String,
                        classname: String,
                        onerror: String? = null,
                        adapter: String? = null,
                        adaptto: String? = null,
                        uri: String? = null,
                        kind: DefinitionKind) {
        val componentHelper = ComponentHelper.getComponentHelper(projectAO)!!
        val definition = componentHelper.getDefinition(name)
        val restrictedDefinitions = componentHelper.getRestrictedDefinitions(name)
        if (definition != null
                || restrictedDefinitions != null
                && (kind == DefinitionKind.TYPE
                        || restrictedDefinitions.firstOrNull { it.className == classname } != null)) {
            return
        }
        var cl: Class<*>? = null
        val al = javaClass.classLoader
        val onErrorIndex = if (onerror != null) {
            OnError(onerror).index
        } else {
            OnError.FAIL
        }
        try {
            try {
                val componentName = ProjectHelper.genComponentName(uri, name)
                if (onErrorIndex != OnError.IGNORE) {
                    cl = Class.forName(classname, true, al)
                }
                val adapterClass = if (adapter != null) {
                    Class.forName(adapter, true, al)
                } else {
                    null
                }
                val adaptToClass = if (adaptto != null) {
                    Class.forName(adaptto, true, al)
                } else {
                    null
                }

                val def = AntTypeDefinition()
                def.name = componentName
                def.className = classname
                def.setClass(cl)
                def.setAdapterClass(adapterClass)
                def.setAdaptToClass(adaptToClass)
                def.isRestrict = kind == DefinitionKind.COMPONENT
                def.classLoader = al
                if (cl != null) {
                    def.checkClass(projectAO)
                }
                componentHelper.addDataTypeDefinition(def)
            } catch (cnfe: ClassNotFoundException) {
                val msg = "Class $classname cannot be found \n using the classloader " + al
                throw BuildException(msg, cnfe)
            } catch (ncdfe: NoClassDefFoundError) {
                val msg = " A class needed by class $classname cannot be found: ${ncdfe.message}" +
                        "\n using the classloader " + al
                throw BuildException(msg, ncdfe)
            }
        } catch (ex: BuildException) {
            when (onErrorIndex) {
                OnError.FAIL_ALL, OnError.FAIL -> throw ex
                OnError.REPORT ->
                    System.err.println(ex.location.toString() + "Warning: " + ex.message)
                else -> System.err.println(ex.location.toString() + ex.message)
            }
        }
    }

    open fun configure() {
        setAttributes()
        if (parentWrapperAO != null) {
            val parent = parentWrapperAO.proxy as UnknownElement
            parent.addChild(taskAO)
            parentWrapperAO.addChild(wrapperAO)
        } else {
            taskAO.maybeConfigure()
        }
    }

    private fun initTaskContainers() {
        if (taskContainers != null) {
            for (entry in taskContainers) {
                val taskContainer = entry.first
                val init = entry.second
                val lazyTask = taskContainer.createLazyTask(init)
                taskContainer.addTask(lazyTask)
            }
        }
    }

    fun addTaskContainer(taskContainer: DSLTaskContainer, init: DSLTaskContainer.() -> Unit) {
        nearestExecutable.taskContainers!!.add(Pair(taskContainer, init))
    }

    fun execute() {
        assert(parentWrapperAO == null)
        initTaskContainers()
        taskAO.perform()
    }
}

abstract class DSLTaskContainer(projectAO: Project, targetAO: Target) : DSLElement(projectAO, targetAO) {
    fun createLazyTask(init: DSLTaskContainer.() -> Unit): LazyTask {
        val lazyTaskAO = LazyTask(this, init)
        lazyTaskAO.project = projectAO
        lazyTaskAO.owningTarget = targetAO
        return lazyTaskAO
    }

    open fun addTask(task: Task) {
        targetAO.addTask(task)
    }
}

abstract class DSLTaskContainerTask(projectAO: Project, targetAO: Target,
                                    parentAO: RuntimeConfigurable?,
                                    elementTag: String,
                                    nearestExecutable: DSLTask?) :
        DSLTask(projectAO, targetAO, parentAO, elementTag, nearestExecutable), DSLTaskContainer {
    override fun addTask(task: Task) {
        (wrapperAO.proxy!! as TaskContainer).addTask(task)
    }
}

open class DSLProject : DSLElement(Project(), Target()), DSLTaskContainer {
    val targets = HashMap<String, DSLTarget>() // field name -> DSLTarget
    private var configured = false

    init {
        projectAO.init()
        projectAO.addBuildListener(createLogger())
        initProperties(projectAO)
        targetAO.project = projectAO
        targetAO.name = ""
    }

    private fun createLogger(): BuildLogger {
        val logger = DefaultLogger()
        val msgOutputLevel = Project.MSG_INFO
        val emacsMode = false
        logger.setMessageOutputLevel(msgOutputLevel)
        logger.setOutputPrintStream(System.out)
        logger.setErrorPrintStream(System.err)
        logger.setEmacsMode(emacsMode)
        return logger
    }

    private fun Class<*>.getTargetFields(): List<Field> {
        return declaredFields!!.filter { it.type!!.name == DSL_TARGET }
    }

    fun configureTargets() {
        if (configured) {
            return
        }
        configured = true
        var klass = javaClass as Class<*>?
        while (klass != null) {
            val targetFieldNames = klass.getTargetFields().map { it.name!! }.valuesToMap { it.toLowerCase() } // names in lower case -> normal names
            val targetGetters = klass.methods!!
                    .filter { it.name!!.startsWith("get") && it.returnType!!.name == DSL_TARGET }
            for (targetGetter in targetGetters) {
                val fieldName = targetFieldNames[targetGetter.name!!.substring("get".length).toLowerCase()]
                if (fieldName != null && !targets.contains(fieldName)) {
                    var target = targetGetter.invoke(this) as DSLTarget
                    if (target.project != this) {
                        val name = if (target.namedAsField) {
                            null
                        } else {
                            target.name
                        }
                        target = target(target.depends, name, target.init)
                    }
                    if (target.namedAsField) {
                        target.name = fieldName
                    }
                    targets[fieldName] = target
                }
            }
            klass = klass.superclass
        }
        for (target in targets.values) {
            target.configure()
        }
    }

    private fun getDefaultName(): String? {
        var klass = javaClass as Class<*>?
        var def: String? = null
        while (def == null && klass != null) {
            for (targetField in klass.getTargetFields()) {
                if (targetField.isAnnotationPresent(default::class.java)) {
                    if (def != null) {
                        throw DSLException("Project cannot have more than one default target")
                    }
                    def = targets[targetField.name]!!.name
                }
            }
            klass = klass.superclass
        }
        return def
    }

    fun perform() {
        configureTargets()
        val default = getDefaultName()
        var error: Throwable? = null
        try {
            projectAO.fireBuildStarted()
            if (default != null) {
                projectAO.setDefault(default)
                val basedir = propertyHelper!!.getProperty("basedir") as String
                projectAO.baseDir = File(basedir)
                projectAO.executeTarget(projectAO.defaultTarget)
            }
        } catch (t: Throwable) {
            error = t
        } finally {
            try {
                projectAO.fireBuildFinished(error)
            } catch (t: Throwable) {
                System.err.println("Caught an exception while logging the"
                        + " end of the build.  Exception was:")
                t.printStackTrace()
                if (error != null) {
                    System.err.println("There has been an error prior to that:")
                    error.printStackTrace()
                }
            }
        }
    }
}

class DSLTarget(val project: DSLProject, var name: String?,
                val depends: Array<out KProperty<DSLTarget>>,
                val init: DSLTaskContainer.() -> Unit) : DSLElement(project.projectAO, Target()), DSLTaskContainer {
    val namedAsField = name == null

    fun configure() {
        targetAO.project = projectAO
        targetAO.name = name
        projectAO.addTarget(name, targetAO)
        val dependsString = StringBuilder()
        for (dependRef in depends) {
            if (dependsString.isNotEmpty()) {
                dependsString.append(",")
            }
            val depend = project.targets[dependRef.name]!!
            dependsString.append(depend.name)
        }
        targetAO.setDepends(dependsString.toString())
        addTask(createLazyTask(init))
    }

    fun execute() {
        project.configureTargets()
        System.out.println("\n$name:")
        targetAO.execute()
    }
}

private fun DSLProject.target(depends: Array<out KProperty<DSLTarget>>, name: String?, init: DSLTaskContainer.() -> Unit): DSLTarget {
    return DSLTarget(this, name, depends, init)
}

fun DSLProject.target(name: String, vararg depends: KProperty<DSLTarget>, init: DSLTaskContainer.() -> Unit): DSLTarget {
    return target(depends, name, init)
}

fun DSLProject.target(vararg depends: KProperty<DSLTarget>, init: DSLTaskContainer.() -> Unit): DSLTarget {
    return target(depends, null, init)
}
