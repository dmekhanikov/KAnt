import jetbrains.kant.dsl.*

val basedir by StringProperty()
val b by StringProperty() { basedir }

object project : DSLProject()

fun box(): String {
    project.perform()
    b
    return "OK"
}
