package testData.DSLGenerator.switch

import ru.ifmo.rain.mekhanikov.antdsl.*

val destFile: String by StringProperty { "" }
val antContribJarFile: String by StringProperty { "" }
val value: String by StringProperty { "" }

fun main(args: Array<String>) {
    project(args) {
        taskdef(resource = "net/sf/antcontrib/antcontrib.properties",
                classpath = antContribJarFile)

        default = target("Test switch") {
            switch(value = value) {
                case(value = "foo") {
                    echo(message = "Value is foo", file = destFile)
                }
                case(value = "bar") {
                    echo(message = "Value is bar", file = destFile)
                }
                default {
                    echo(message = "Value is neither foo nor bar", file = destFile)
                }
            }
        }
    }
}
