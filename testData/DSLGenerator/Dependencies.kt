import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.copy
import jetbrains.kant.dsl.types.fileset

val src1Dir by StringProperty()
val src2Dir by StringProperty()
val srcDir by StringProperty()
val destDir by StringProperty()

val dependenciesProject = object : DSLProject() {
    {
        default = ::testDepends
    }

    val copy1 = target {
        copy(todir = srcDir) {
            fileset(dir = src1Dir)
        }
    }

    val copy2 = target {
        copy(todir = srcDir) {
            fileset(dir = src2Dir)
        }
    }

    val testDepends = target(::copy1, ::copy2) {
        copy(todir = destDir) {
            fileset(dir = srcDir)
        }
    }
}
