import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.other.net.sf.antcontrib.math.*
import jetbrains.kant.dsl.other.net.sf.antcontrib.property.propertycopy
import jetbrains.kant.dsl.taskdefs.condition.*

val result by DoubleProperty()

object project : DSLProject() {
    {
        if (istrue(true)) {
            math(result = "fact") {
                op(op = "*") {
                    num(value = "1")
                    num(value = "2")
                    num(value = "3")
                    num(value = "4")
                    num(value = "5")
                }
            }
            propertycopy(property = "result", from = "fact")
        }
    }
}

fun box(): String {
    project.perform()
    if (result != 120.0) {
        return "fail"
    }
    return "OK"
}
