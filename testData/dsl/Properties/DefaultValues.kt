import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.other.net.sf.antcontrib.property.propertycopy

val stringSrc by StringProperty() { "test" }
val stringDst by StringProperty()
val intSrc by IntProperty() { 42 }
val intDst by IntProperty()
val doubleProp by DoubleProperty() { 146.47 }

object project : DSLProject() {
    {
        propertycopy(from = "stringSrc", property = "stringDst")
        propertycopy(from = "intSrc", property = "intDst")
    }
}

fun box(): String {
    project.perform()
    return if (stringDst != "test" || intDst != 42 || doubleProp != 146.47) {
        "fail"
    } else {
        "OK"
    }
}
