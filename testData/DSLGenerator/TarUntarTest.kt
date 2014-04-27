package testData.DSLGenerator.tarUntar

import ru.ifmo.rain.mekhanikov.antdsl.*

val sourceDir: String by StringProperty("")
val tarFile: String by StringProperty("")
val outDir: String by StringProperty("")

fun main(args: Array<String>) {
    project(args) {
        default = target("Tar and untar") {
            sequential {
                tar (basedir = sourceDir) {
                    destfile = tarFile
                }
                untar {
                    src = tarFile
                    dest = outDir
                }
            }
        }
    }
}