import jetbrains.kant.dsl.*

var result = ""

object project : DSLProject() {
    val otherTarget = target {
        result = "other"
    }

    [default]
    val defaultTarget = target {
        result = "default"
    }

    val oneMoreTarget = target {
        result = "one more"
    }
}

fun box(): String {
    project.perform()
    return if (result != "default") {
        result
    } else {
        "OK"
    }
}
