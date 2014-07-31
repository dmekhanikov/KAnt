package testData.DSLGenerator.zipUnzip

import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.*

val sourceDir by StringProperty()
val zipFile by StringProperty()
val outDir by StringProperty()

fun main(args: Array<String>) {
    project(args) {
        default = target("Zip and unzip") {
            zip (basedir = sourceDir) {
                destfile = zipFile
            }
            unzip {
                src = zipFile
                dest = outDir
            }
        }
    }
}
