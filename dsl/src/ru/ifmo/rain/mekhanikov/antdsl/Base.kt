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

abstract class DSLElement(val elementTag: String) {
    val attributes = TreeMap<String, Any?>()
    val children = ArrayList<DSLElement>();

    public fun initElement<T : DSLElement>(element: T, init: T.() -> Unit): T {
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
            if (attr.key.equals("id")) {
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

class DSLProject() : DSLElement("project") {
    var default: DSLTarget? = null
    var basedir: File by Delegates.mapVar(attributes);

    override fun perform(parentWrapper: RuntimeConfigurable?, project: Project?, target: Target?) {
        [suppress("NAME_SHADOWING")]
        val project = Project()
        project.init()
        val implicitTarget = Target()
        implicitTarget.setProject(project);
        implicitTarget.setName("");
        for (child in children) {
            child.perform(null, project, implicitTarget)
        }
        implicitTarget.execute()
        if (default != null) {
            project.setDefault(default!!.targetName)
            if (attributes.containsKey("basedir")) {
                project.setBaseDir(basedir)
            }
            project.executeTarget(project.getDefaultTarget())
        }
    }
}

abstract class DSLTaskContainer(elementTag: String) : DSLElement(elementTag)

class DSLTarget(val targetName: String) : DSLTaskContainer("target") {
    override fun perform(parentWrapper: RuntimeConfigurable?, project: Project?, target: Target?) {
        [suppress("NAME_SHADOWING")]
        val target = Target()
        project!!.addTarget(targetName, target)
        for (child in children) {
            child.perform(null, project, target)
        }
    }
}

public fun project(args: Array<String>, init: DSLProject.() -> Unit): DSLProject {
    val dslProject = DSLProject()
    initProperties(args)
    dslProject.init()
    dslProject.perform(null, null, null)
    return dslProject
}

public fun DSLProject.target(name: String, init: DSLTarget.() -> Unit): DSLTarget {
    val dslTarget = DSLTarget(name)
    initElement(dslTarget, init)
    return dslTarget
}