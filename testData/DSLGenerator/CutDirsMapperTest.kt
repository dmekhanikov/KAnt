package testData.DSLGenerator.cutdirsmapper

import ru.ifmo.rain.mekhanikov.antdsl.*

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
