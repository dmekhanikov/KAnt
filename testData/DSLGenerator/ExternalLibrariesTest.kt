package testData.DSLGenerator.externalLibraries

import ru.ifmo.rain.mekhanikov.antdsl.*

val value: String by StringProperty { "" }
val destFile: String by StringProperty { "" }
val antContribJarFile: String by StringProperty { "" }

fun main(args: Array<String>) {
    project(args) {
        taskdef(resource = "net/sf/antcontrib/antcontrib.properties",
                classpath = antContribJarFile)

        default = target("Test foreach") {
            switch(value = value) {
                case(value = "foo") {
                    echo(file = destFile, message = "Value is foo")
                }
                case(value = "bar") {
                    echo(file = destFile, message = "Value is bar")
                }
                default {
                    echo(file = destFile, message = "Value is neither foo nor bar")
                }
            }
        }
    }
}
