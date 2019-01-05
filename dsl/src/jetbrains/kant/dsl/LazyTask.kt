package jetbrains.kant.dsl

import org.apache.tools.ant.Task

class LazyTask(private val taskContainer: DSLTaskContainer, private val init: DSLTaskContainer.() -> Unit) : Task() {
    override fun execute() {
        taskContainer.init()
    }
}
