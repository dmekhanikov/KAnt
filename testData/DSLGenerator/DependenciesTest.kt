package testData.DSLGenerator.dependencies

import ru.ifmo.rain.mekhanikov.antdsl.*
import ru.ifmo.rain.mekhanikov.antdsl.taskdefs.copy
import ru.ifmo.rain.mekhanikov.antdsl.types.fileset

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
