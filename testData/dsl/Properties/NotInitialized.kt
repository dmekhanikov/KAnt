import jetbrains.kant.dsl.*

val prop by StringProperty()

object project : DSLProject()

fun box(): String {
    project.perform()
    try {
        prop
        return "false"
    } catch (e: IllegalStateException) {
        return "OK"
    }
}
