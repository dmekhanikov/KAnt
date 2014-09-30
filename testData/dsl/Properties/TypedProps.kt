import jetbrains.kant.dsl.*

import jetbrains.kant.test.PropertyManager
import jetbrains.kant.test.resource

val propertyManager = PropertyManager()

val stringProperty by StringProperty()
val booleanProperty by BooleanProperty()
val intProperty by IntProperty()
val doubleProperty by DoubleProperty()

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
        return if (stringProperty != "string property" || booleanProperty != true ||
                intProperty != 42 || doubleProperty != 21568.3) {
            "fail"
        } else {
            "OK"
        }
    } finally {
        propertyManager.restore()
    }
}
