import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.echo
import jetbrains.kant.dsl.taskdefs.replace
import jetbrains.kant.dsl.taskdefs.replaceToken
import jetbrains.kant.dsl.taskdefs.replaceValue

import jetbrains.kant.test.file
import jetbrains.kant.test.readFile

object project : DSLProject() {
    {
        echo(message = "dog cat lion", file = file("test.txt"))
        replace(file = file("test.txt")) {
            replaceToken {
                text { "cat" }
            }
            replaceValue {
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