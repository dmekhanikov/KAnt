package testData.DSLGenerator.mkdir

import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.*

val dir by StringProperty()

fun main(args: Array<String>) {
    object : DSLProject() {
        {
            default = ::mkdirTest
        }
        val mkdirTest = target("Mkdir test") {
            mkdir(dir = dir)
        }

        val delete = target {
            delete(dir = dir)
        }
    }.perform()
}
