import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.taskdef

import jetbrains.kant.gtcommon.constants.ANT_CONTRIB_JAR

var result = ""

object project : DSLProject() {
    {
        taskdef(resource = "net/sf/antcontrib/antcontrib.properties",
                classpath = ANT_CONTRIB_JAR)
        task(name = "switch") {
            attribute("value", "bar")
            element("case") {
                attribute("value", "foo")
                nestedTasks {
                    result = "Value is foo"
                }
            }
            element("case") {
                attribute("value", "bar")
                nestedTasks {
                    result = "Value is bar"
                }
            }
            element("default") {
                nestedTasks {
                    result = "Value is neither foo nor bar"
                }
            }
        }
    }
}

fun box(): String {
    project.perform()
    return if (result != "Value is bar") {
        result
    } else {
        "OK"
    }
}
