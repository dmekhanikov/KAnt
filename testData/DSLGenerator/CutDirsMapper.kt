import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.copy
import jetbrains.kant.dsl.types.fileset
import jetbrains.kant.dsl.types.mappers.cutdirsmapper

val srcDir by StringProperty()
val destDir by StringProperty()

object cutDirsMapperProject : DSLProject() {
    {
        default = ::testCutDirsMapper
    }
    val testCutDirsMapper = target {
        copy(todir = destDir) {
            fileset(dir = srcDir)
            cutdirsmapper(dirs = 2)
        }
    }
}
