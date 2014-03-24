package testData.DSLGenerator.mkdir

import ru.ifmo.rain.mekhanikov.antdsl.AntProperty
import ru.ifmo.rain.mekhanikov.antdsl.project
import ru.ifmo.rain.mekhanikov.antdsl.target
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.mkdir
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.delete
import java.io.File

val dir : String by AntProperty<String>()

fun main(args : Array<String>) {
    project(args) {
        default = target("Mkdir test") {
            mkdir(dir = dir)
        }

        target("Delete") {
            delete(dir = dir)
        }
    }
}