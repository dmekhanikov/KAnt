package jetbrains.kant.dsl

import org.apache.tools.ant.Task

public class LazyTask(private val taskContainer: DSLTaskContainer, private val init: DSLTaskContainer.() -> Unit) : Task() {
    override public fun execute() {
        taskContainer.init()
    }
}
