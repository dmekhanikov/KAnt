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

abstract class DSLElement(elementTag: String) {
    val elementTag = elementTag
    val children = ArrayList<DSLElement>()
    val attributes = TreeMap<String, Any?>()

    public fun initElement<T: DSLElement>(element: T, init: T.() -> Unit): T {
        element.init()
        children.add(element)
        return element
    }
}

class DSLProject() : DSLElement("project") {
    val project = Project(); { project.init() }

    var default : DSLTarget? = null
    var basedir : File by Delegates.mapVar(attributes);

    public fun target(name : String, init : DSLTarget.() -> Unit) : DSLTarget {
        val dslTarget = DSLTarget(name)
        initElement(dslTarget, init)
        project.fireBuildStarted()
        dslTarget.target.setProject(project)
        for (child in dslTarget.children) {
            val task = UnknownElement(child.elementTag)
            task.setProject(project)
            task.setQName(child.elementTag)
            task.setTaskType(ProjectHelper.genComponentName("", child.elementTag))
            task.setTaskName(child.elementTag)
            task.setOwningTarget(dslTarget.target)
            dslTarget.target.addTask(task)
            val wrapper = RuntimeConfigurable(task, task.getTaskName())
            for (attr in child.attributes) {
                wrapper.setAttribute(attr.getKey(), attr.getValue())
            }
        }
        project.addTarget(name, dslTarget.target)
        return dslTarget
    }
}

class DSLTarget(targetName: String) : DSLElement("target") {
    val targetName = targetName
    val target = Target()
}

public fun project(init : DSLProject.() -> Unit) : DSLProject {
    val dslProject = DSLProject()
    dslProject.init()
    if (dslProject.default == null) {
        return dslProject
    }
    dslProject.project.setDefault(dslProject.default?.targetName)
    if (dslProject.attributes.containsKey("basedir")) {
        dslProject.project.setBaseDir(dslProject.basedir)
    }
    dslProject.project.executeTarget(dslProject.project.getDefaultTarget())
    return dslProject
}