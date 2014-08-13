package testData.DSLGenerator.zipUnzip

import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.*

val sourceDir by StringProperty()
val zipFile by StringProperty()
val outDir by StringProperty()

fun main(args: Array<String>) {
    object : DSLProject() {
        {
            default = ::zipUnzip
        }
        val zipUnzip = target("Zip and unzip") {
            zip (basedir = sourceDir) {
                destFile = zipFile
            }
            unzip {
                src = zipFile
                dest = outDir
            }
        }
    }.perform()
}
