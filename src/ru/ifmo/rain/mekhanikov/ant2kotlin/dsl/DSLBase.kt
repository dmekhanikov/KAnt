package ru.ifmo.rain.mekhanikov.ant2kotlin.dsl

import java.util.ArrayList
import java.util.TreeMap
import org.apache.tools.ant.Target
import org.apache.tools.ant.Project
import org.apache.tools.ant.UnknownElement
import org.apache.tools.ant.ProjectHelper
import org.apache.tools.ant.RuntimeConfigurable
import kotlin.properties.Delegates
import java.io.File

abstract class DSLElement(tag: String) {
    val tag = tag
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

    var default : String by Delegates.mapVar(attributes)
    var basedir : File by Delegates.mapVar(attributes);

    public fun target(name : String, init : DSLTarget.() -> Unit) : DSLTarget {
        val dslTarget = DSLTarget(name)
        initElement(dslTarget, init)
        project.fireBuildStarted()
        dslTarget.target.setProject(project)
        for (child in dslTarget.children) {
            val task = UnknownElement(child.tag)
            task.setProject(project)
            task.setQName(child.tag)
            task.setTaskType(ProjectHelper.genComponentName("", child.tag))
            task.setTaskName(child.tag)
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

class DSLTarget(name : String) : DSLElement("target") {
    val name = name
    val target = Target()
}

public fun project(init : DSLProject.() -> Unit) : DSLProject {
    val dslProject = DSLProject()
    dslProject.init()
    if (!dslProject.attributes.containsKey("default")) {
        return dslProject
    }
    dslProject.project.setDefault(dslProject.default)
    if (dslProject.attributes.containsKey("basedir")) {
        dslProject.project.setBaseDir(dslProject.basedir)
    }
    dslProject.project.executeTarget(dslProject.project.getDefaultTarget())
    return dslProject
}