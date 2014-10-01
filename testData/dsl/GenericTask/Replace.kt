import jetbrains.kant.dsl.*

import jetbrains.kant.test.file
import jetbrains.kant.test.readFile

object project : DSLProject() {
    {
        task("echo") {
            attribute("message", "dog cat lion")
            attribute("file", file("test.txt"))
        }
        task("replace") {
            attribute("file", file("test.txt"))
            element("replaceToken") {
                text { "cat" }
            }
            element("replaceValue") {
                text { "wombat" }
            }
        }
    }
}

fun box(): String {
    project.perform()
    val result = readFile(file("test.txt")).trim()
    return if (result != "dog wombat lion") {
        result
    } else {
        "OK"
    }
}
