import jetbrains.kant.dsl.*

var result = ""

object project : DSLProject() {
    [default]
    val target1 = target(::target2, ::target3) {
        result += "target1"
    }

    val target2 = target() {
        result += "target2"
    }

    val target3: DSLTarget = target() {
        result += "target3"
    }
}

fun box(): String {
    project.perform()
    return if (result != "target2target3target1") {
        result
    } else {
        "OK"
    }
}
