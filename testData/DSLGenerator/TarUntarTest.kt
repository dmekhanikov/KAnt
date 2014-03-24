package testData.DSLGenerator.tarUntar

import ru.ifmo.rain.mekhanikov.antdsl.AntProperty
import ru.ifmo.rain.mekhanikov.antdsl.project
import ru.ifmo.rain.mekhanikov.antdsl.target
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.sequential
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.tar
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.untar
import java.io.File

val sourceDir : String by AntProperty<String>()
val tarFile : String by AntProperty<String>()
val outDir : String by AntProperty<String>()

fun main(args : Array<String>) {
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