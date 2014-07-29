package testData.DSLGenerator.cutdirsmapper

import ru.ifmo.rain.mekhanikov.antdsl.*
import ru.ifmo.rain.mekhanikov.antdsl.taskdefs.copy
import ru.ifmo.rain.mekhanikov.antdsl.types.fileset
import ru.ifmo.rain.mekhanikov.antdsl.types.mappers.cutdirsmapper

val srcDir by StringProperty()
val destDir by StringProperty()

fun main(args: Array<String>) {
    project(args) {
        default = target(name = "Test cutdirsmapper") {
            copy(todir = destDir) {
                fileset(dir = srcDir)
                cutdirsmapper(dirs = 2)
            }
        }
    }
}
