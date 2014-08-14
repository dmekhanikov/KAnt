package jetbrains.kant.dsl

import org.apache.tools.ant.*
import java.io.File
import java.util.ArrayList
import java.util.HashMap
import java.lang.reflect.Field
import kotlin.reflect.KMemberProperty
import jetbrains.kant.common.valuesToMap

private val DSL_TARGET = javaClass<DSLTarget>().getName()

public abstract class DSLElement(val projectAO: Project, val targetAO: Target)

public open class DSLTask(projectAO: Project, targetAO: Target,
                       val parentWrapperAO: RuntimeConfigurable?, // if it is null then it can be executed
                       val elementTag: String,
                       nearestExecutable: DSLTask?) : DSLElement(projectAO, targetAO) {
    val taskAO: UnknownElement
    val wrapperAO: RuntimeConfigurable
    val attributes = HashMap<String, Any?>()
    val taskContainers: ArrayList<Pair<DSLTaskContainer, DSLTaskContainer.() -> Unit>>?
    val nearestExecutable: DSLTask
    var nestedText: String? = null;
    {
        taskAO = UnknownElement(elementTag)
        taskAO.setQName(elementTag)
        taskAO.setTaskType(ProjectHelper.genComponentName("", elementTag))
        taskAO.setTaskName(elementTag)
        taskAO.setProject(projectAO)
        taskAO.setOwningTarget(targetAO)
        wrapperAO = RuntimeConfigurable(taskAO, taskAO.getTaskName())
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

    public fun defineType(className: String) {
        val componentHelper = ComponentHelper.getComponentHelper(projectAO)!!
        if (componentHelper.getDefinition(elementTag) != null) {
            return
        }
        val typedef = DSLTask(projectAO, targetAO, null, "typedef", null)
        typedef.attributes["name"] = elementTag
        typedef.attributes["classname"] = className
        typedef.configure()
        typedef.execute()
    }

    public fun defineComponent(className: String) {
        val componentHelper = ComponentHelper.getComponentHelper(projectAO)!!
        val definitions = componentHelper.getRestrictedDefinitions(elementTag)
        if (definitions != null && definitions.contains(className) || componentHelper.getDefinition(elementTag) != null) {
            return
        }
        val componentdef = DSLTask(projectAO, targetAO, null, "componentdef", null)
        componentdef.attributes["name"] = elementTag
        componentdef.attributes["classname"] = className
        componentdef.configure()
        componentdef.execute()
    }

    open public fun configure() {
        setAttributes()
        if (parentWrapperAO != null) {
            val parent = parentWrapperAO.getProxy() as UnknownElement
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

    public fun addTaskContainer(taskContainer: DSLTaskContainer, init: DSLTaskContainer.() -> Unit) {
        nearestExecutable.taskContainers!!.add(Pair(taskContainer, init))
    }

    public fun execute() {
        assert(parentWrapperAO == null)
        initTaskContainers()
        taskAO.perform()
    }
}

public trait DSLTaskContainer : DSLElement {
    public fun createLazyTask(init: DSLTaskContainer.() -> Unit): LazyTask {
        val lazyTaskAO = LazyTask(this, init)
        lazyTaskAO.setProject(projectAO)
        lazyTaskAO.setOwningTarget(targetAO)
        return lazyTaskAO
    }

    public open fun addTask(task: Task) {
        targetAO.addTask(task)
    }
}

public abstract class DSLTaskContainerTask(projectAO: Project, targetAO: Target,
                                    parentAO: RuntimeConfigurable?,
                                    elementTag: String,
                                    nearestExecutable: DSLTask?) : DSLTask(projectAO, targetAO, parentAO, elementTag, nearestExecutable), DSLTaskContainer {
    override public fun addTask(task: Task) {
        (wrapperAO.getProxy()!! as TaskContainer).addTask(task)
    }
}

public open class DSLProject : DSLElement(Project(), Target()), DSLTaskContainer {
    val targets = HashMap<String, DSLTarget>() // field name -> DSLTarget
    private var configured = false
    {
        initProperties(projectAO)
        targetAO.setProject(projectAO)
        targetAO.setName("")
        projectAO.init()
        projectAO.addBuildListener(createLogger())
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
        return getDeclaredFields()!!.filter { it.getType()!!.getName() == DSL_TARGET }
    }

    public fun configureTargets() {
        if (configured) {
            return
        }
        configured = true
        var klass = javaClass as Class<Any?>?
        while (klass != null) {
            val targetFieldNames = klass!!.getTargetFields().map { it.getName()!! }.
                    valuesToMap { it.toLowerCase() } // names in lower case -> normal names
            val targetGetters = klass!!.getMethods()!!
                    .filter { it.getName()!!.startsWith("get") && it.getReturnType()!!.getName() == DSL_TARGET }
            for (targetGetter in targetGetters) {
                val fieldName = targetFieldNames[targetGetter.getName()!!.substring("get".length).toLowerCase()]
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
            klass = klass!!.getSuperclass()
        }
        for (target in targets.values()) {
            target.configure()
        }
    }

    private fun getDefaultName(): String? {
        var klass = javaClass as Class<Any?>?
        var default: String? = null
        while (default == null && klass != null) {
            for (targetField in klass!!.getTargetFields()) {
                if (targetField.isAnnotationPresent(javaClass<default>())) {
                    if (default != null) {
                        throw DSLException("Project cannot have more than one default target")
                    }
                    default = targets[targetField.getName()]!!.name
                }
            }
            klass = klass!!.getSuperclass()
        }
        return default
    }

    public fun perform() {
        configureTargets()
        val default = getDefaultName()
        var error: Throwable? = null
        try {
            projectAO.fireBuildStarted()
            if (default != null) {
                projectAO.setDefault(default)
                val basedir = propertyHelper!!.getProperty("basedir") as String
                projectAO.setBaseDir(File(basedir))
                projectAO.executeTarget(projectAO.getDefaultTarget())
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
                    error!!.printStackTrace()
                }
            }
        }
    }
}

public class DSLTarget(val project: DSLProject, var name: String?,
                val depends: Array<KMemberProperty<out DSLProject, DSLTarget>>,
                val init: DSLTaskContainer.() -> Unit) : DSLElement(project.projectAO, Target()), DSLTaskContainer {
    val namedAsField = name == null

    public fun configure() {
        targetAO.setProject(projectAO)
        targetAO.setName(name)
        projectAO.addTarget(name, targetAO)
        val dependsString = StringBuilder()
        for (dependRef in depends) {
            if (dependsString.length() > 0) {
                dependsString.append(",")
            }
            val depend = project.targets[dependRef.name]!!
            dependsString.append(depend.name)
        }
        targetAO.setDepends(dependsString.toString())
        addTask(createLazyTask(init))
    }

    public fun execute() {
        project.configureTargets()
        System.out.println("\n$name:")
        targetAO.execute()
    }
}

private fun DSLProject.target(depends: Array<KMemberProperty<out DSLProject, DSLTarget>>, name: String?, init: DSLTaskContainer.() -> Unit): DSLTarget {
    return DSLTarget(this, name, depends, init)
}

public fun DSLProject.target(name: String, vararg depends: KMemberProperty<out DSLProject, DSLTarget>, init: DSLTaskContainer.() -> Unit): DSLTarget {
    return target(depends, name, init)
}

public fun DSLProject.target(vararg depends: KMemberProperty<out DSLProject, DSLTarget>, init: DSLTaskContainer.() -> Unit): DSLTarget {
    return target(depends, null, init)
}
