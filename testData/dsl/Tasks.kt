import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.echo
import jetbrains.kant.dsl.taskdefs.zip
import jetbrains.kant.dsl.taskdefs.unzip
import jetbrains.kant.test.file
import jetbrains.kant.test.readFile

object project : DSLProject() {
    {
        echo(message = "TestTESTteSt", file = file("src/test.txt"))
        zip(basedir = file("src/"), destFile = file("zip.zip"))
        unzip(src = file("zip.zip"), dest = file("dst/"))
    }
}

fun box(): String {
    project.perform()
    val actual = readFile(file("dst/test.txt")).trim()
    if (actual != "TestTESTteSt") {
        return actual
    }
    return "OK"
}