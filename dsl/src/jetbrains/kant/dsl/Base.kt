package jetbrains.kant.dsl

import org.apache.tools.ant.*
import kotlin.properties.Delegates
import java.io.File
import java.util.ArrayList
import java.util.HashMap
import kotlin.reflect.KMemberProperty
import kotlin.reflect.jvm.javaGetter
import java.lang.reflect.Method
import java.lang.reflect.Field

val DSL_TARGET = "jetbrains.kant.dsl.DSLTarget"

abstract class DSLElement(val projectAO: Project, val targetAO: Target)

open class DSLTask(projectAO: Project, targetAO: Target,
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

trait DSLTaskContainer : DSLElement {
    fun createLazyTask(init: DSLTaskContainer.() -> Unit): LazyTask {
        val lazyTaskAO = LazyTask(this, init)
        lazyTaskAO.setProject(projectAO)
        lazyTaskAO.setOwningTarget(targetAO)
        return lazyTaskAO
    }

    open fun addTask(task: Task) {
        targetAO.addTask(task)
    }
}

abstract class DSLTaskContainerTask(projectAO: Project, targetAO: Target,
                                    parentAO: RuntimeConfigurable?,
                                    elementTag: String,
                                    nearestExecutable: DSLTask?) : DSLTask(projectAO, targetAO, parentAO, elementTag, nearestExecutable), DSLTaskContainer {
    override public fun addTask(task: Task) {
        (wrapperAO.getProxy()!! as TaskContainer).addTask(task)
    }
}

open class DSLProject : DSLElement(Project(), Target()), DSLTaskContainer {
    var default: KMemberProperty<out DSLProject, DSLTarget>? = null
    val targets = HashMap<String, DSLTarget>();
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

    private fun <K, V>List<V>.valuesToMap(key: (V) -> K): Map<K, V> {
        val result = HashMap<K, V>()
        for (value in this) {
            result[key(value)] = value
        }
        return result
    }

    private fun Class<*>.getTargetNames(): Map<String, String> {
        val names = ArrayList<String>()
        var klass = this as Class<Any?>?
        while (klass != null) {
            names.addAll(klass!!.getDeclaredFields()!!.
                    filter { it.getType()!!.getName() == DSL_TARGET }.
                    map { it.getName()!! })
            klass = klass!!.getSuperclass()
        }
        return names.valuesToMap { it.toLowerCase() }
    }

    private fun configureTargets() {
        val targetNames = javaClass.getTargetNames()
        val targetGetters = javaClass.getMethods()!!
                .filter { it.getName()!!.startsWith("get") && it.getReturnType()!!.getName() == DSL_TARGET }
        for (targetGetter in targetGetters) {
            val fieldName = targetNames[targetGetter.getName()!!.substring("get".length).toLowerCase()]
            if (fieldName != null) {
                val target = targetGetter.invoke(this) as DSLTarget
                if (target.name == null) {
                    target.name = fieldName
                }
                targets[fieldName] = target
            }
        }
        for (target in targets.values()) {
            target.configure()
        }
    }

    public fun perform() {
        configureTargets()
        var error: Throwable? = null
        try {
            projectAO.fireBuildStarted()
            if (default != null) {
                val defaultName = (default!!.javaGetter!!.invoke(this) as DSLTarget).name
                projectAO.setDefault(defaultName)
                val basedir = propertyHelper!!.getProperty("basedir") as String
                projectAO.setBaseDir(File(basedir))
                projectAO.executeTarget(projectAO.getDefaultTarget())
            }
        } catch (t: Throwable) {
            error = t
        } finally {
            try {
                projectAO.fireBuildFinished(error);
            } catch (t: Throwable) {
                System.err.println("Caught an exception while logging the"
                        + " end of the build.  Exception was:");
                t.printStackTrace();
                if (error != null) {
                    System.err.println("There has been an error prior to"
                            + " that:");
                    error!!.printStackTrace();
                }
            }
        }
    }
}

class DSLTarget(val project: DSLProject, var name: String?,
                val depends: Array<KMemberProperty<out DSLProject, DSLTarget>>,
                val init: DSLTaskContainer.() -> Unit) : DSLElement(project.projectAO, Target()), DSLTaskContainer {
    fun configure() {
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
