import jetbrains.kant.dsl.*

import jetbrains.kant.test.file
import jetbrains.kant.test.readFile

object project : DSLProject() {
    {
        task("echo") {
            attribute("message", "TestTestTest")
            attribute("file", file("foo/bar/baz/test.txt"))
        }
        task("copy") {
            attribute("todir", file("dst"))
            element("fileset") {
                attribute("dir", file("foo"))
            }
            element("cutdirsmapper") {
                attribute("dirs", "2")
            }
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
