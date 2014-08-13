package testData.DSLGenerator.copy

import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.*
import jetbrains.kant.dsl.types.fileset

val srcDir by StringProperty()
val destDir by StringProperty()

fun main(args: Array<String>) {
    object : DSLProject(args) {
        {
            default = ::copyTest
        }
        val filesToCopy = fileset(dir = srcDir)

        val copyTest = target("Copy test") {
            retry(retryCount = 3) {
                copy(todir = destDir) {
                    fileset(refid = filesToCopy)
                }
            }
        }
    }.perform()
}
