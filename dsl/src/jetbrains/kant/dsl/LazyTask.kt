package jetbrains.kant.dsl

import org.apache.tools.ant.Task

class LazyTask(val taskContainer: DSLTaskContainer, val init: DSLTaskContainer.() -> Unit) : Task() {
    override public fun execute() {
        taskContainer.init()
    }
}