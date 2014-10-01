import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.taskdef

import jetbrains.kant.gtcommon.constants.ANT_CONTRIB_JAR
import jetbrains.kant.test.file
import jetbrains.kant.test.readFile

val fact by DoubleProperty()

object project : DSLProject() {
    {
        taskdef(resource = "net/sf/antcontrib/antcontrib.properties",
                classpath = ANT_CONTRIB_JAR)
        task("math") {
            attribute("result", "fact")
            element("op") {
                attribute("op", "*")
                element("num") {
                    attribute("value", "1")
                }
                element("num") {
                    attribute("value", "2")
                }
                element("num") {
                    attribute("value", "3")
                }
                element("num") {
                    attribute("value", "4")
                }
                element("num") {
                    attribute("value", "5")
                }
            }
        }
    }
}

fun box(): String {
    project.perform()
    return if (fact != 120.0) {
        fact.toString()
    } else {
        "OK"
    }
}
