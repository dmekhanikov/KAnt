package testData.DSLGenerator.math

import ru.ifmo.rain.mekhanikov.antdsl.*

val destFile: String by StringProperty { "" }
val antContribJarFile: String by StringProperty { "" }
val fact: Double by DoubleProperty { 0.0 }

fun main(args: Array<String>) {
    project(args) {
        taskdef(resource = "net/sf/antcontrib/antcontrib.properties",
                classpath = antContribJarFile)

        default = target("Test math") {
            math(result = "fact") {
                op(op = "*") {
                    num(value = "1")
                    num(value = "2")
                    num(value = "3")
                    num(value = "4")
                    num(value = "5")
                }
            }
            echo(message = "$fact", file = destFile)
        }
    }
}
