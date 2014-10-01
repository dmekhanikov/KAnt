package jetbrains.kant.dsl

import org.apache.tools.ant.Project
import org.apache.tools.ant.Target
import org.apache.tools.ant.RuntimeConfigurable

public class DSLGenericTask(projectAO: Project, targetAO: Target,
                  parentWrapperAO: RuntimeConfigurable?,
                  elementTag: String,
                  nearestExecutable: DSLTask?) :
        DSLTaskContainerTask(projectAO, targetAO, parentWrapperAO, elementTag, nearestExecutable), DSLTextContainer {
    public fun attribute(key: String, value: String) {
        attributes[key] = value
    }

    public fun element(name: String, init: DSLGenericTask.() -> Unit) {
        val dslObject = DSLGenericTask(projectAO, targetAO, wrapperAO, name, nearestExecutable)
        dslObject.init()
        dslObject.configure()
    }

    public fun nestedTasks(tasks: DSLTaskContainer.() -> Unit) {
        addTaskContainer(this, tasks)
    }
}

public fun DSLTaskContainer.task(name: String, init: DSLGenericTask.() -> Unit) {
    val dslObject = DSLGenericTask(projectAO, targetAO, null, name, null)
    dslObject.init()
    dslObject.configure()
    dslObject.execute()
}
