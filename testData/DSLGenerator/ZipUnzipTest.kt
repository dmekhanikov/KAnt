package testData.DSLGenerator.zipUnzip

import ru.ifmo.rain.mekhanikov.antdsl.*

val sourceDir: String by StringProperty("")
val zipFile: String by StringProperty("")
val outDir: String by StringProperty("")

fun main(args: Array<String>) {
    project(args) {
        default = target("Zip and unzip") {
            sequential {
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
}
