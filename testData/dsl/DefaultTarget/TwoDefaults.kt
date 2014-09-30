import jetbrains.kant.dsl.*

object project : DSLProject() {
    default val default1 = target {}
    default val default2 = target {}
}

fun box(): String {
    try {
        project.perform()
        return "fail"
    } catch(e: DSLException) {
        return "OK"
    }
}
