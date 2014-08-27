import jetbrains.kant.dsl.*

var result = ""

object project1 : DSLProject() {
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

object project2 : DSLProject() {
    default val default1 = target {}
    default val default2 = target {}
}

open class SuperProject : DSLProject() {
    default val superDefaultTarget = target {
        result = "super default"
    }

    val superOtherTarget = target {
        result = "super other"
    }

    val superOneMoreTarget = target {
        result = "super one more"
    }
}

object childProject1 : SuperProject() {
    default val childDefaultTarget = target {
        result = "child default"
    }

    val childOtherTarget = target {
        result = "child other"
    }

    val childOneMoreTarget = target {
        result = "child one more"
    }
}

object childProject2 : SuperProject() {
    val childDefaultTarget = target {
        result = "child default"
    }

    val childOtherTarget = target {
        result = "child other"
    }

    val childOneMoreTarget = target {
        result = "child one more"
    }
}

fun box(): String {
    project1.perform()
    if (result != "default") {
        return "project1: $result"
    }
    try {
        project2.perform()
        return "project2: fail"
    } catch (e: DSLException) {}
    childProject1.perform()
    if (result != "child default") {
        return "childProject1: $result"
    }
    childProject2.perform()
    if (result != "super default") {
        return "childProject2: $result"
    }
    return "OK"
}
