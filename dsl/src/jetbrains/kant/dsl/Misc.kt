package jetbrains.kant.dsl

import org.apache.tools.ant.taskdefs.condition.Condition
import org.apache.tools.ant.taskdefs.ConditionTask

var maxRefid = 0

class DSLReference<T : DSLTask>(public val value: T) {
    val refid = (++maxRefid).toString();
    {
        value.attributes["id"] = refid
    }
}

class DSLLoaderRef(val value : String)

trait DSLTextContainer: DSLTask

public fun DSLTextContainer.text(init: DSLTextContainer.() -> String) {
    nestedText = init()
}

trait DSLCondition: DSLTask {
    override fun configure() {
        setAttributes()
        val parent = DSLTask(projectAO, targetAO, null, "condition", null)
        parent.taskAO.addChild(taskAO)
        parent.wrapperAO.addChild(wrapperAO)
        parent.configure()
    }

    public fun eval(): Boolean {
        return (taskAO.getWrapper()!!.getProxy() as Condition).eval()
    }
}
