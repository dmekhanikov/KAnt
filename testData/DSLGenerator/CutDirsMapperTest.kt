package testData.DSLGenerator.cutdirsmapper

import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.copy
import jetbrains.kant.dsl.types.fileset
import jetbrains.kant.dsl.types.mappers.cutdirsmapper

val srcDir by StringProperty()
val destDir by StringProperty()

fun main(args: Array<String>) {
    object : DSLProject() {
        {
            default = ::testCutDirsMapper
        }
        val testCutDirsMapper = target(name = "Test cutdirsmapper") {
            copy(todir = destDir) {
                fileset(dir = srcDir)
                cutdirsmapper(dirs = 2)
            }
        }
    }.perform()
}
