import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.*
import jetbrains.kant.dsl.types.fileset

val srcDir by StringProperty()
val destDir by StringProperty()

object copyProject : DSLProject() {
    val filesToCopy = fileset(dir = srcDir)

    [default]
    val testCopy = target {
        retry(retryCount = 3) {
            copy(todir = destDir) {
                fileset(refid = filesToCopy)
            }
        }
    }
}
