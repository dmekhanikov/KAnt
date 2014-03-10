package testData.DSLGenerator.tarUntar

import ru.ifmo.rain.mekhanikov.antdsl.project
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.tar
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.untar
import java.io.File

fun main(args : Array<String>) {
    project {
        default = "Tar and untar"
        target("Tar and untar") {
            tar {
                basedir = File(args[0])
                destfile = File(args[1])
            }
            untar {
                src = File(args[1])
                dest = File(args[2])
            }
        }
    }
}