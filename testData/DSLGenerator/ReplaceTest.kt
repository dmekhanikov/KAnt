package testData.DSLGenerator.replace

import ru.ifmo.rain.mekhanikov.antdsl.*
import ru.ifmo.rain.mekhanikov.antdsl.taskdefs.*

val file by StringProperty()

fun main(args: Array<String>) {
    project(args) {
        echo(message = "cat", file = file)
        replace(file = file) {
            replacetoken {
                text { "cat" }
            }
            replacevalue {
                text { "wombat" }
            }
        }
    }
}
