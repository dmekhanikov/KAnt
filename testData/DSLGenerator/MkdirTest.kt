package testData.DSLGenerator.mkdir

import ru.ifmo.rain.mekhanikov.antdsl.project
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.mkdir
import java.io.File

fun main(args : Array<String>) {
    project {
        default = target("Mkdir test") {
            mkdir (dir = File(args[0]))
        }
    }
}