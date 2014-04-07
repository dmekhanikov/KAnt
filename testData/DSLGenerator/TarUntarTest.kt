package testData.DSLGenerator.tarUntar

import ru.ifmo.rain.mekhanikov.antdsl.StringProperty
import ru.ifmo.rain.mekhanikov.antdsl.project
import ru.ifmo.rain.mekhanikov.antdsl.target
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.sequential
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.tar
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.untar
import java.io.File

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