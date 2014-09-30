import jetbrains.kant.dsl.*

val antVersion by StringProperty("ant.version")
val antJavaVersion by StringProperty("ant.java.version")

object project : DSLProject()

fun box(): String {
    project.perform()
    return if (antVersion == "" || antJavaVersion == "") {
        "fail"
    } else {
        "OK"
    }
}
