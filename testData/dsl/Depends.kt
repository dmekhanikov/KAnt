import jetbrains.kant.dsl.*

var result = StringBuilder()

fun append(s: String) {
    if (result.length() != 0) {
        result.append(' ')
    }
    result.append(s)
}

fun clear() {
    result = StringBuilder()
}

object project1 : DSLProject() {
    [default]
    val target1 = target(::target2) {
        append("target1")
    }

    val target2 = target(::target3) {
        append("target2")
    }

    val target3: DSLTarget = target() {
        append("target3")
    }
}

object project2 : DSLProject() {
    [default]
    val target1 = target(::target2, ::target3) {
        append("target1")
    }

    val target2 = target() {
        append("target2")
    }

    val target3: DSLTarget = target() {
        append("target3")
    }
}

object project3 : DSLProject() {
    [default]
    val target1 = target(::target2) {
        append("target1")
    }

    val target2 = target(::target3) {
        append("target2")
    }

    val target3: DSLTarget = target(::target1) {
        append("target3")
    }
}

fun box(): String {
    project1.perform()
    if (result.toString() != "target3 target2 target1") {
        return "project1: $result"
    }
    clear()
    project2.perform()
    if (result.toString() != "target2 target3 target1") {
        return "project2: $result"
    }
    clear()
    project3.perform()
    if (result.length() != 0) {
        return "project3: $result"
    }
    return "OK"
}
