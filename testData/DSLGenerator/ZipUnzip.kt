import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.*

val sourceDir by StringProperty()
val zipFile by StringProperty()
val outDir by StringProperty()

object zipUnzipProject : DSLProject() {
    {
        default = ::zipAndUnzip
    }
    val zipAndUnzip = target {
        zip (basedir = sourceDir) {
            destFile = zipFile
        }
        unzip {
            src = zipFile
            dest = outDir
        }
    }
}
