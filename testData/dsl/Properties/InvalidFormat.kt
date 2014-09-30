import jetbrains.kant.dsl.*

import jetbrains.kant.test.PropertyManager

val propertyManager = PropertyManager()

val intProp by IntProperty("doubleProperty")
val doubleProp by DoubleProperty("stringProperty")
val booleanProp by BooleanProperty("intProperty")
val shortProp by ShortProperty() { 1234 }
val stringProp by StringProperty("shortProp")

object project : DSLProject()

fun init() {
    propertyManager.setProperty("stringProperty", "string property")
    propertyManager.setProperty("booleanProperty", "true")
    propertyManager.setProperty("intProperty", "42")
    propertyManager.setProperty("doubleProperty", "21568.3")
}

fun box(): String {
    try {
        init()
        project.perform()
        try {
            intProp
            return "intProp"
        } catch (e: Exception) {}
        try {
            doubleProp
            return "doubleProp"
        } catch (e: Exception) {}
        if (booleanProp) {
            return "booleanProp"
        }
        try {
            stringProp
            return "stringProp"
        } catch (e: Exception) {}
    } finally {
        propertyManager.restore()
    }
    return "OK"
}
