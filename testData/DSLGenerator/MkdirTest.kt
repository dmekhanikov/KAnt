package testData.DSLGenerator.mkdir

import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.*

val dir by StringProperty()

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
