import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.other.net.sf.antcontrib.math.*

import jetbrains.kant.test.file
import jetbrains.kant.test.readFile

val fact by DoubleProperty()

object project : DSLProject() {
    {
        math(result = "fact") {
            op(op = "*") {
                num(value = "1")
                num(value = "2")
                num(value = "3")
                num(value = "4")
                num(value = "5")
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