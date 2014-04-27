package testData.DSLGenerator.properties

import ru.ifmo.rain.mekhanikov.antdsl.*

val systemPropertiesOutFile: String by StringProperty("")
val userPropertiesOutFile: String by StringProperty("")

val antVersion: String by StringProperty("", "ant.version")
val stringProperty: String by StringProperty("")
val booleanProperty: Boolean by BooleanProperty(false)
val intProperty: Int by IntProperty(0)
val doubleProperty: Double by DoubleProperty(0.0)
val defaultProperty: String by StringProperty("default value")

fun main(args: Array<String>) {
    project(args) {
        default = target("Properties test") {
            echo(message = antVersion, file = systemPropertiesOutFile)
            val message = "$stringProperty\n$booleanProperty\n$intProperty\n$doubleProperty\n$defaultProperty"
            echo(message = message, file = userPropertiesOutFile)
        }
    }
}
