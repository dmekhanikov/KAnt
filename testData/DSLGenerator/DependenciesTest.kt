package testData.DSLGenerator.dependencies

import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.copy
import jetbrains.kant.dsl.types.fileset

val src1Dir by StringProperty()
val src2Dir by StringProperty()
val srcDir by StringProperty()
val destDir by StringProperty()

fun main(args: Array<String>) {
    project(args) {
        val copy1 = target(name = "copy1") {
            copy(todir = srcDir) {
                fileset(dir = src1Dir)
            }
        }

        val copy2 = target(name = "copy2") {
            copy(todir = srcDir) {
                fileset(dir = src2Dir)
            }
        }

        default = target("Test depends", copy1, copy2) {
            copy(todir = destDir) {
                fileset(dir = srcDir)
            }
        }
    }
}
