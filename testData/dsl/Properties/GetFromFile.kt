import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.property

import jetbrains.kant.test.resource

val stringFileProperty by StringProperty("string.file.property")
val intFileProperty by IntProperty("int.file.property")
val doubleFileProperty by DoubleProperty("double.file.property")

object project : DSLProject() {
    {
        property(file = resource("dsl/Properties/GetFromFile.txt"))
    }
}

fun box(): String {
    project.perform()
    return if (stringFileProperty != "string file property" ||
            intFileProperty != 9000 || doubleFileProperty != 146.47) {
        "fail"
    } else {
        "OK"
    }
}
