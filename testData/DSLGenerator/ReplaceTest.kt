package testData.DSLGenerator.replace

import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.*

val file by StringProperty()

fun main(args: Array<String>) {
    project(args) {
        echo(message = "cat", file = file)
        replace(file = file) {
            replaceToken {
                text { "cat" }
            }
            replaceValue {
                text { "wombat" }
            }
        }
    }
}
