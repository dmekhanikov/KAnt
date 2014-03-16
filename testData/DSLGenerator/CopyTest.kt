package testData.DSLGenerator.copy

import ru.ifmo.rain.mekhanikov.antdsl.project
import ru.ifmo.rain.mekhanikov.antdsl.target
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.copy
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.fileset
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.retry
import java.io.File

fun main(args : Array<String>) {
    project {
        default = target("copy test") {
            retry(retrycount = 3) {
                copy(todir = File(args[1])) {
                    fileset(dir = File(args[0]))
                }
            }
        }
    }
}