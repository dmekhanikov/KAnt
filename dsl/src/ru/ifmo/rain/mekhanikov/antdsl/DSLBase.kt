package ru.ifmo.rain.mekhanikov.antdsl

import java.util.ArrayList
import java.util.TreeMap
import org.apache.tools.ant.Target
import org.apache.tools.ant.Project
import org.apache.tools.ant.UnknownElement
import org.apache.tools.ant.ProjectHelper
import org.apache.tools.ant.RuntimeConfigurable
import kotlin.properties.Delegates
import java.io.File
import java.util.HashMap
import java.util.regex.Pattern

abstract class DSLElement(elementTag: String) {
    val elementTag = elementTag
    val attributes = TreeMap<String, Any?>()
    val children = ArrayList<DSLElement>();

    public fun initElement<T: DSLElement>(element: T, init: T.() -> Unit): T {
        element.init()
        children.add(element)
        return element
    }

    open public fun perform(parentWrapper : RuntimeConfigurable?, project : Project?, target : Target?, isNested : Boolean) {
        val wrapper : RuntimeConfigurable?
        if (isNested) {
            val task = UnknownElement(elementTag)
            task.setProject(project)
            task.setQName(elementTag)
            task.setTaskType(ProjectHelper.genComponentName("", elementTag))
            task.setTaskName(elementTag)
            task.setOwningTarget(target)
            wrapper = RuntimeConfigurable(task, task.getTaskName())
            val parent = parentWrapper?.getProxy() as UnknownElement?
            if (parent != null) {
                parent.addChild(task)
            } else {
                target!!.addTask(task)
            }
            parentWrapper?.addChild(wrapper)
            for (attr in attributes) {
                wrapper!!.setAttribute(attr.getKey(), attr.getValue())
            }
        } else {
            wrapper = null
        }
        for (child in children) {
            child.perform(wrapper, project, target, true)
        }
    }
}

class DSLProject() : DSLElement("project") {
    var default : DSLTarget? = null
    var basedir : File by Delegates.mapVar(attributes);

    override fun perform(parentWrapper : RuntimeConfigurable?, project: Project?, target : Target?, isNested : Boolean) {
        [suppress("NAME_SHADOWING")]
        val project = Project()
        project.init()
        for (child in children) {
            child.perform(null, project, null, false)
        }
        if (default != null) {
            project.setDefault(default!!.targetName)
            if (attributes.containsKey("basedir")) {
                project.setBaseDir(basedir)
            }
            project.executeTarget(project.getDefaultTarget())
        }
    }
}

abstract class DSLTaskContainer(elementTag: String): DSLElement(elementTag) {}

class DSLTarget(targetName: String) : DSLTaskContainer("target") {
    val targetName = targetName

    override fun perform(parentWrapper : RuntimeConfigurable?, project : Project?, target : Target?, isNested : Boolean) {
        [suppress("NAME_SHADOWING")]
        val target = Target()
        project!!.addTarget(targetName, target)
        for (child in children) {
            child.perform(null, project, target, true)
        }
    }
}

public fun project(args : Array<String>, init : DSLProject.() -> Unit) : DSLProject {
    val dslProject = DSLProject()
    initProperties(args)
    dslProject.init()
    dslProject.perform(null, null, null, false)
    return dslProject
}

public fun DSLProject.target(name : String, init : DSLTarget.() -> Unit) : DSLTarget {
    val dslTarget = DSLTarget(name)
    initElement(dslTarget, init)
    return dslTarget
}

