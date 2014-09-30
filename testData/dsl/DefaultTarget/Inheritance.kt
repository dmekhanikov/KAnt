import jetbrains.kant.dsl.*

var result = ""

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
        result = "child1 default"
    }

    val childOtherTarget = target {
        result = "child1 other"
    }

    val childOneMoreTarget = target {
        result = "child1 one more"
    }
}

object childProject2 : SuperProject() {
    val childDefaultTarget = target {
        result = "child2 default"
    }

    val childOtherTarget = target {
        result = "child2 other"
    }

    val childOneMoreTarget = target {
        result = "child2 one more"
    }
}

fun box(): String {
    childProject1.perform()
    if (result != "child1 default") {
        return result
    }
    childProject2.perform()
    if (result != "super default") {
        return result
    }
    return "OK"
}
