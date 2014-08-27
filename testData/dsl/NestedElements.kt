import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.*
import jetbrains.kant.dsl.types.fileset
import jetbrains.kant.dsl.types.mappers.cutdirsmapper
import jetbrains.kant.dsl.other.net.sf.antcontrib.math.*
import jetbrains.kant.dsl.other.net.sf.antcontrib.logic.*
import jetbrains.kant.test.file
import jetbrains.kant.test.readFile
import jetbrains.kant.gtcommon.constants.ANT_CONTRIB_JAR

val fact by DoubleProperty()
var result = ""

object project : DSLProject() {
    {
        taskdef(resource = "net/sf/antcontrib/antcontrib.properties",
                classpath = ANT_CONTRIB_JAR)
    }
    val testCutDirsMapper = target {
        echo(message = "TestTestTest", file = file("foo/bar/baz/test.txt"))
        copy(todir = file("dst")) {
            fileset(dir = file("foo"))
            cutdirsmapper(dirs = 2)
        }
    }

    val testReplace = target {
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

    val testMath = target {
        math(result = "fact") {
            op(op = "*") {
                num(value = "1")
                num(value = "2")
                num(value = "3")
                num(value = "4")
                num(value = "5")
            }
        }
    }

    val testSwitch = target {
        switch(value = "bar") {
            case(value = "foo") {
                result = "Value is foo"
            }
            case(value = "bar") {
                result = "Value is bar"
            }
            default {
                result = "Value is neither foo nor bar"
            }
        }
    }
}

fun box(): String {
    project.testCutDirsMapper.execute()
    var actual = readFile(file("dst/test.txt")).trim()
    if (actual != "TestTestTest") {
        return "cutdirsmapper: $actual"
    }
    project.testReplace.execute()
    actual = readFile(file("test.txt")).trim()
    if (actual != "dog wombat lion") {
        return "replace: $actual"
    }
    project.testMath.execute()
    if (fact != 120.0) {
        return "math: $fact"
    }
    project.testSwitch.execute()
    if (result != "Value is bar") {
        return "switch: $result"
    }
    return "OK"
}
