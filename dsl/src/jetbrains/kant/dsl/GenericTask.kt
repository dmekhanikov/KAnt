package jetbrains.kant.dsl

import org.apache.tools.ant.Project
import org.apache.tools.ant.RuntimeConfigurable
import org.apache.tools.ant.Target

class DSLGenericTask(projectAO: Project, targetAO: Target,
                     parentWrapperAO: RuntimeConfigurable?,
                     elementTag: String,
                     nearestExecutable: DSLTask?) :
        DSLTaskContainerTask(projectAO, targetAO, parentWrapperAO, elementTag, nearestExecutable), DSLTextContainer {
    fun attribute(key: String, value: String) {
        attributes[key] = value
    }

    fun element(name: String, init: DSLGenericTask.() -> Unit) {
        val dslObject = DSLGenericTask(projectAO, targetAO, wrapperAO, name, nearestExecutable)
        dslObject.init()
        dslObject.configure()
    }

    fun nestedTasks(tasks: DSLTaskContainer.() -> Unit) {
        addTaskContainer(this, tasks)
    }
}

fun DSLTaskContainer.task(name: String, init: DSLGenericTask.() -> Unit) {
    val dslObject = DSLGenericTask(projectAO, targetAO, null, name, null)
    dslObject.init()
    dslObject.configure()
    dslObject.execute()
}
