package testData.DSLGenerator.copy

import ru.ifmo.rain.mekhanikov.antdsl.StringProperty
import ru.ifmo.rain.mekhanikov.antdsl.project
import ru.ifmo.rain.mekhanikov.antdsl.target
import ru.ifmo.rain.mekhanikov.antdsl.generated.types.fileset
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.fileset
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.copy
import ru.ifmo.rain.mekhanikov.antdsl.generated.taskdefs.retry
import java.io.File

val srcDir: String by StringProperty("")
val destDir: String by StringProperty("")

fun main(args: Array<String>) {
    project(args) {
        val filesToCopy = fileset(dir = srcDir)

        default = target("copy test") {
            retry(retrycount = 3) {
                copy(todir = destDir) {
                    fileset(refid = filesToCopy)
                }
            }
        }
    }
}
