import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.*

val dir by StringProperty()

val mkdirProject = object : DSLProject() {
    {
        default = ::testMkdir
    }
    val testMkdir = target {
        mkdir(dir = dir)
    }

    val delete = target {
        delete(dir = dir)
    }
}
