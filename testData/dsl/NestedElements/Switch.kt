import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.other.net.sf.antcontrib.logic.*

import jetbrains.kant.test.file
import jetbrains.kant.test.readFile

var result = ""

object project : DSLProject() {
    {
        switch(value = "bar") {
            case(value = "foo") {
                result = "Value is foo"
            }
            case(value = "bar") {
                result = "Value is bar"
            }
            default {
                result = "Value is neither foo nor bar"
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