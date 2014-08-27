import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.property

val prop1 by StringProperty()
val prop2 by StringProperty()

var result = ""

object project : DSLProject() {
    {
        property(name = "prop1", value = "init")
        result = prop1
    }

    default val testConsistency = target {
        property(name = "prop2", value = "target")
        result += " $prop2"
    }
}

fun box(): String {
    project.perform()
    if (result != "init target") {
        return result
    }
    return "OK"
}
