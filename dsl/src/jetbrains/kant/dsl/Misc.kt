package jetbrains.kant.dsl

import org.apache.tools.ant.taskdefs.condition.Condition
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

private var maxRefid = 0

public class DSLReference<T : DSLTask>(public val value: T) {
    public val refid: String = (++maxRefid).toString();
    {
        value.attributes["id"] = refid
    }

    public fun <Q: DSLTask> asExpected(): DSLReference<Q> {
        return this as DSLReference<Q>;
    }
}

[Retention(RetentionPolicy.RUNTIME)]
public annotation class default

public class DSLException(message: String) : Exception(message)

public trait DSLTextContainer: DSLTask {
    public fun text(init: DSLTextContainer.() -> String) {
        nestedText = init()
    }
}

public trait DSLCondition: DSLTask {
    override public fun configure() {
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
