package testData.DSLGenerator.copy

import ru.ifmo.rain.mekhanikov.antdsl.*
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
