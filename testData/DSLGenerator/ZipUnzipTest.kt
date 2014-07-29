package testData.DSLGenerator.zipUnzip

import ru.ifmo.rain.mekhanikov.antdsl.*
import ru.ifmo.rain.mekhanikov.antdsl.taskdefs.*

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
