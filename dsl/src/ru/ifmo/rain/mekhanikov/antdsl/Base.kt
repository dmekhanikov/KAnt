package ru.ifmo.rain.mekhanikov.antdsl

import org.apache.tools.ant.*
import kotlin.properties.Delegates
import java.io.File
import java.util.ArrayList
import java.util.HashMap

abstract class DSLElement(val projectAO: Project, val targetAO: Target) {
    val attributes = HashMap<String, Any?>()
}

abstract class DSLTask(projectAO: Project, targetAO: Target,
                       val parentWrapperAO: RuntimeConfigurable?, // if it is null then it can be executed
                       val elementTag: String,
                       nearestExecutable: DSLTask?) : DSLElement(projectAO, targetAO) {
    val taskAO: UnknownElement
    val wrapperAO: RuntimeConfigurable
    val taskContainers: ArrayList<Pair<DSLTaskContainer, DSLTaskContainer.() -> Unit>>?
    val nearestExecutable: DSLTask;
    {
        taskAO = UnknownElement(elementTag)
        taskAO.setProject(projectAO)
        taskAO.setQName(elementTag)
        taskAO.setTaskType(ProjectHelper.genComponentName("", elementTag))
        taskAO.setTaskName(elementTag)
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

    public fun configure() {
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
        (taskAO as Task).perform()
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

class DSLProject(val args: Array<String>) : DSLElement(Project(), Target()), DSLTaskContainer {
    var default: DSLTarget? = null
    var basedir: String by Delegates.mapVar(attributes)
    val targets = ArrayList<DSLTarget>();
    {
        targetAO.setProject(projectAO)
        targetAO.setName("")
        projectAO.init()
        projectAO.addBuildListener(createLogger())
        initProperties(projectAO, args)
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

    fun perform() {
        if (default != null) {
            projectAO.setDefault(default!!.name)
            if (attributes.containsKey("basedir")) {
                projectAO.setBaseDir(File(basedir))
            }
            projectAO.executeTarget(projectAO.getDefaultTarget())
        }
    }
}

class DSLTarget(projectAO: Project, val name: String,
                val depends: Array<DSLTarget>,
                val init: DSLTaskContainer.() -> Unit) : DSLElement(projectAO, Target()), DSLTaskContainer {
    {
        targetAO.setProject(projectAO)
        targetAO.setName(name)
        projectAO.addTarget(name, targetAO)
        val dependsString = StringBuilder()
        for (depend in depends) {
            if (dependsString.length() > 0) {
                dependsString.append(",")
            }
            dependsString.append(depend.name)
        }
        targetAO.setDepends(dependsString.toString())
        addTask(createLazyTask(init))
    }
}

public fun project(args: Array<String>, init: DSLProject.() -> Unit): DSLProject {
    val dslProject = DSLProject(args)
    var error: Throwable? = null
    try {
        dslProject.projectAO.fireBuildStarted()
        dslProject.init()
        dslProject.perform()
    } catch (t: Throwable) {
        error = t
    } finally {
        try {
            dslProject.projectAO.fireBuildFinished(error);
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
    return dslProject
}

public fun DSLProject.target(name: String, vararg depends: DSLTarget, init: DSLTaskContainer.() -> Unit): DSLTarget {
    return DSLTarget(projectAO, name, depends, init)
}
