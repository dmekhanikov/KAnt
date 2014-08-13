package testData.DSLGenerator.switch

import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.*
import jetbrains.kant.dsl.other.net.sf.antcontrib.logic.*

val destFile by StringProperty()
val antContribJarFile by StringProperty()
val value by StringProperty()

fun main(args: Array<String>) {
    object : DSLProject() {
        {
            taskdef(resource = "net/sf/antcontrib/antcontrib.properties",
                    classpath = antContribJarFile)
            default = ::testSwitch
        }

        val testSwitch = target("Test switch") {
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
    }.perform()
}
