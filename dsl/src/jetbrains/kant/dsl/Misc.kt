package jetbrains.kant.dsl

import org.apache.tools.ant.taskdefs.condition.Condition

private var maxRefid = 0

class DSLReference<T : DSLTask>(val value: T) {
    val refid: String = (++maxRefid).toString()

    init {
        value.attributes["id"] = refid
    }

    fun <Q : DSLTask> asExpected(): DSLReference<Q> {
        return this as DSLReference<Q>
    }
}

@Retention(AnnotationRetention.RUNTIME)
annotation class default

class DSLException(message: String) : Exception(message)

abstract class DSLTextContainer : DSLTask {
    fun text(init: DSLTextContainer.() -> String) {
        nestedText = init()
    }
}

abstract class DSLCondition : DSLTask {
    override fun configure() {
        setAttributes()
        val parent = DSLTask(projectAO, targetAO, null, "condition", null)
        parent.taskAO.addChild(taskAO)
        parent.wrapperAO.addChild(wrapperAO)
        parent.configure()
    }

    fun eval(): Boolean {
        return (taskAO.wrapper!!.proxy as Condition).eval()
    }
}
