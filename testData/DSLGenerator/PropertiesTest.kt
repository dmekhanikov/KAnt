package testData.DSLGenerator.properties

import ru.ifmo.rain.mekhanikov.antdsl.*

val systemPropertiesOutFile by StringProperty()
val userPropertiesOutFile by StringProperty()
val propertiesFile by StringProperty()

val antVersion by StringProperty("ant.version")
val stringProperty by StringProperty()
val booleanProperty by BooleanProperty()
val intProperty by IntProperty()
val doubleProperty by DoubleProperty()
val defaultProperty by StringProperty() { "default value" }
val stringFileProperty by StringProperty("string.file.property")
val intFileProperty by IntProperty("int.file.property")
val doubleFileProperty by DoubleProperty("double.file.property")

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
