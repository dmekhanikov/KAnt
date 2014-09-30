import jetbrains.kant.dsl.*

var result = ""

object project : DSLProject() {
    [default]
    val target1 = target(::target2) {
        result += "target1"
    }

    val target2 = target(::target3) {
        result += "target2"
    }

    val target3: DSLTarget = target(::target1) {
        result += "target3"
    }
}

fun box(): String {
    project.perform()
    return if (result != "") {
        result
    } else {
        "OK"
    }
}
