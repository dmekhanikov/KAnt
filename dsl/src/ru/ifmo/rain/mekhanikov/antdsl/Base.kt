package ru.ifmo.rain.mekhanikov.antdsl

import org.apache.tools.ant.Target
import org.apache.tools.ant.Project
import org.apache.tools.ant.UnknownElement
import org.apache.tools.ant.ProjectHelper
import org.apache.tools.ant.RuntimeConfigurable
import kotlin.properties.Delegates
import java.io.File
import java.util.ArrayList
import java.util.TreeMap
import java.util.HashMap

abstract class DSLElement {
    val attributes = TreeMap<String, Any?>()
    val children = ArrayList<DSLElement>()
    var elementTag: String? = null

    public fun initElement<T : DSLElement>(elementTag: String, element: T, init: T.() -> Unit): T {
        element.elementTag = elementTag
        element.init()
        children.add(element)
        return element
    }

    open public fun perform(parentWrapper: RuntimeConfigurable?, project: Project?, target : Target?) {
        val task = UnknownElement(elementTag)
        task.setProject(project)
        task.setQName(elementTag)
        task.setTaskType(ProjectHelper.genComponentName("", elementTag))
        task.setTaskName(elementTag)
        task.setOwningTarget(target)
        val parent = parentWrapper?.getProxy() as UnknownElement?
        if (parent != null) {
            parent.addChild(task)
        } else {
            target!!.addTask(task)
        }
        val wrapper = RuntimeConfigurable(task, task.getTaskName())
        for (attr in attributes) {
            if (attr.key == "id") {
                project!!.addIdReference(attr.value as String, task)
            }
            val storedValue = attr.value
            val value: Any? =
                    if (storedValue is Reference<*>) {
                        storedValue.refid
                    } else {
                        storedValue
                    }
            wrapper.setAttribute(attr.key, value)
        }
        parentWrapper?.addChild(wrapper)
        for (child in children) {
            child.perform(wrapper, project, target)
        }
    }
}

abstract class DSLTaskContainer : DSLElement()

class DSLProject(val args: Array<String>) : DSLTaskContainer() {
    var default: DSLTarget? = null
    var basedir: String by Delegates.mapVar(attributes)
    val targets = HashMap<DSLTarget, DSLTarget.() -> Unit>()
    val project = Project();
    {
        project.init()
        initProperties(project, args)
    }

    override fun perform(parentWrapper: RuntimeConfigurable?, project: Project?, target: Target?) {
        val implicitTarget = Target()
        implicitTarget.setProject(this.project)
        implicitTarget.setName("")
        for (child in children) {
            child.perform(null, this.project, implicitTarget)
        }
        implicitTarget.execute()
        for (entry in targets) {
            val target = entry.key
            val init = entry.value
            initElement("target", target, init)
            target.perform(null, this.project, null)
        }
        if (default != null) {
            this.project.setDefault(default!!.name)
            if (attributes.containsKey("basedir")) {
                this.project.setBaseDir(File(basedir))
            }
            this.project.executeTarget(this.project.getDefaultTarget())
        }
    }
}

class DSLTarget(val name: String, val depends: Array<DSLTarget>) : DSLTaskContainer() {
    override fun perform(parentWrapper: RuntimeConfigurable?, project: Project?, target: Target?) {
        val target = Target()
        target.setProject(project)
        target.setName(name)
        project!!.addTarget(name, target)
        val dependsString = StringBuilder()
        for (depend in depends) {
            if (dependsString.length() > 0) {
                dependsString.append(",")
            }
            dependsString.append(depend.name)
        }
        target.setDepends(dependsString.toString())
        for (child in children) {
            child.perform(null, project, target)
        }
    }
}

public fun project(args: Array<String>, init: DSLProject.() -> Unit): DSLProject {
    val dslProject = DSLProject(args)
    dslProject.init()
    dslProject.perform(null, null, null)
    return dslProject
}

public fun DSLProject.target(name: String, vararg depends: DSLTarget, init: DSLTarget.() -> Unit): DSLTarget {
    val dslTarget = DSLTarget(name, depends)
    targets.put(dslTarget, init)
    return dslTarget
}
