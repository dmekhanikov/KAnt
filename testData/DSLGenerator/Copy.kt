import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.*
import jetbrains.kant.dsl.types.fileset

val srcDir by StringProperty()
val destDir by StringProperty()

val copyProject = object : DSLProject() {
    {
        default = ::testCopy
    }
    val filesToCopy = fileset(dir = srcDir)

    val testCopy = target {
        retry(retryCount = 3) {
            copy(todir = destDir) {
                fileset(refid = filesToCopy)
            }
        }
    }
}
