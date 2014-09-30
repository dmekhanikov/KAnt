import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.property
import jetbrains.kant.dsl.other.net.sf.antcontrib.property.propertycopy
import jetbrains.kant.test.PropertyManager
import jetbrains.kant.test.resource

val propertyManager = PropertyManager()

val stringProperty by StringProperty()
val booleanProperty by BooleanProperty()
val intProperty by IntProperty()
val doubleProperty by DoubleProperty()
val defaultProperty by StringProperty { "default value" }
val copyDestination by StringProperty()
val stringFileProperty by StringProperty("string.file.property")
val intFileProperty by IntProperty("int.file.property")
val doubleFileProperty by DoubleProperty("double.file.property")
val systemProperty by StringProperty("ant.version")

var result = ""

object project : DSLProject() {
    {
        propertycopy(property = "copyDestination", from = "defaultProperty")
        property(file = resource("dsl/Properties.txt"))
        result = "\"$stringProperty\", \"$booleanProperty\", \"$intProperty\", \"$doubleProperty\", " +
                "\"$defaultProperty\", \"$copyDestination\", \"$stringFileProperty\", \"$intFileProperty\", " +
                "\"$doubleFileProperty\", \"${getIntProperty("intPropertyByName")}\""
    }
}

fun init() {
    propertyManager.setProperty("stringProperty", "string property")
    propertyManager.setProperty("booleanProperty", "true")
    propertyManager.setProperty("intProperty", "42")
    propertyManager.setProperty("doubleProperty", "21568.3")
    propertyManager.setProperty("intPropertyByName", "73218")
}

fun box(): String {
    try {
        init()
        project.perform()
        if (systemProperty == "") {
            return "systemProperty is empty"
        }
        if (result != "\"string property\", \"true\", \"42\", \"21568.3\", " +
                "\"default value\", \"default value\", \"string file property\", \"9000\", \"146.47\", " +
                "\"73218\"") {
            return result
        }
        return "OK"
    } finally {
        propertyManager.restore()
    }
}
