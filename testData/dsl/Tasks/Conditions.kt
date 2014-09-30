import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.condition.*

var result = ""

object project : DSLProject() {
    {
        result += istrue(true)
        result += " " + matches(string = "Hello, World!", pattern = ".*World!")
        result += " " + equals(arg1 = "15", arg2 = "fifteen")
    }
}

fun box(): String {
    project.perform()
    if (result != "true true false") {
        return result
    }
    return "OK"
}
