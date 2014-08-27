import jetbrains.kant.dsl.*

var result = ""

object project : DSLProject() {
    val base = target {
        result = "OK"
    }
}

object project1 : DSLProject() {
    default val imported = project.base
}

object project2 : DSLProject() {
    default val importedOneMoreTime = project1.imported
}

fun box(): String {
    project1.perform()
    if (result != "OK") {
        return "project1"
    }
    result = ""
    project2.perform()
    if (result != "OK") {
        return "project2"
    }
    return "OK"
}
