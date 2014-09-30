import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.taskdef
import jetbrains.kant.dsl.taskdefs.property
import jetbrains.kant.dsl.other.net.sf.antcontrib.property.propertycopy
import jetbrains.kant.gtcommon.constants.ANT_CONTRIB_JAR

val dst by StringProperty()

object project : DSLProject() {
    {
        taskdef(resource = "net/sf/antcontrib/antcontrib.properties",
                classpath = ANT_CONTRIB_JAR)
        property(name = "src", value = "TestTestTest")
        propertycopy(name = "dst", from = "src")
    }
}

fun box(): String {
    project.perform()
    if (dst != "TestTestTest") {
        return dst
    }
    return "OK"
}
