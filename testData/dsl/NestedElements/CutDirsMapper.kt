import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.echo
import jetbrains.kant.dsl.taskdefs.copy
import jetbrains.kant.dsl.types.fileset
import jetbrains.kant.dsl.types.mappers.cutdirsmapper

import jetbrains.kant.test.file
import jetbrains.kant.test.readFile

object project : DSLProject() {
    {
        echo(message = "TestTestTest", file = file("foo/bar/baz/test.txt"))
        copy(todir = file("dst")) {
            fileset(dir = file("foo"))
            cutdirsmapper(dirs = 2)
        }
    }
}

fun box(): String {
    project.perform()
    val result = readFile(file("dst/test.txt")).trim()
    return if (result != "TestTestTest") {
        result
    } else {
        "OK"
    }
}
