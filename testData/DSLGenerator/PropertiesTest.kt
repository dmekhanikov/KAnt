package testData.DSLGenerator.properties

import ru.ifmo.rain.mekhanikov.antdsl.*

val systemPropertiesOutFile: String by StringProperty { "" }
val userPropertiesOutFile: String by StringProperty { "" }
val propertiesFile: String by StringProperty { "" }

val antVersion: String by StringProperty("ant.version") { "" }
val stringProperty: String by StringProperty { "" }
val booleanProperty: Boolean by BooleanProperty { false }
val intProperty: Int by IntProperty { 0 }
val doubleProperty: Double by DoubleProperty { 0.0 }
val defaultProperty: String by StringProperty { "default value" }
val stringFileProperty: String by StringProperty("string.file.property") { "" }
val intFileProperty: Int by IntProperty("int.file.property") { 0 }
val doubleFileProperty: Double by DoubleProperty("double.file.property") { 0.0 }

fun main(args: Array<String>) {
    project(args) {
        property(file = propertiesFile)
        default = target("Properties test") {
            echo(message = antVersion, file = systemPropertiesOutFile)
            val message = "$stringProperty\n$booleanProperty\n$intProperty\n$doubleProperty\n$defaultProperty\n" +
                            "$stringFileProperty\n$intFileProperty\n$doubleFileProperty\n"
            echo(message = message, file = userPropertiesOutFile)
        }
    }
}
