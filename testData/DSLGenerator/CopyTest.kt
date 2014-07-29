package testData.DSLGenerator.copy

import ru.ifmo.rain.mekhanikov.antdsl.*
import ru.ifmo.rain.mekhanikov.antdsl.taskdefs.*
import ru.ifmo.rain.mekhanikov.antdsl.types.fileset

val srcDir by StringProperty()
val destDir by StringProperty()

fun main(args: Array<String>) {
    project(args) {
        val filesToCopy = fileset(dir = srcDir)

        default = target("Copy test") {
            retry(retrycount = 3) {
                copy(todir = destDir) {
                    fileset(refid = filesToCopy)
                }
            }
        }
    }
}
