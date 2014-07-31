package testData.DSLGenerator.copy

import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.*
import jetbrains.kant.dsl.types.fileset

val srcDir by StringProperty()
val destDir by StringProperty()

fun main(args: Array<String>) {
    project(args) {
        val filesToCopy = fileset(dir = srcDir)

        default = target("Copy test") {
            retry(retrycount = 3) {
                copy(todir = destDir) {
                    fileset(refid = filesToCopy)
                }
            }
        }
    }
}
