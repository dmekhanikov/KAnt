package testData.DSLGenerator.mkdir

import ru.ifmo.rain.mekhanikov.antdsl.*

val dir: String by StringProperty("")

fun main(args: Array<String>) {
    project(args) {
        default = target("Mkdir test") {
            mkdir(dir = dir)
        }

        target("Delete") {
            delete(dir = dir)
        }
    }
}