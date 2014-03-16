package testData.DSLGenerator.tarUntar

import ru.ifmo.rain.mekhanikov.antdsl.project
import ru.ifmo.rain.mekhanikov.antdsl.target
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.sequential
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.tar
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.untar
import java.io.File

fun main(args : Array<String>) {
    project {
        default = target("Tar and untar") {
            sequential {
                tar (basedir = File(args[0])) {
                    destfile = File(args[1])
                }
                untar {
                    src = File(args[1])
                    dest = File(args[2])
                }
            }
        }
    }
}