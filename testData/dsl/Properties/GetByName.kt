import jetbrains.kant.dsl.*

import jetbrains.kant.test.PropertyManager
import jetbrains.kant.test.resource

val propertyManager = PropertyManager()

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
        return if (getStringProperty("stringProperty") != "string property" ||
                getBooleanProperty("booleanProperty") != true ||
                getIntProperty("intProperty") != 42 ||
                getDoubleProperty("doubleProperty") != 21568.3) {
            "fail"
        } else {
            "OK"
        }
    } finally {
        propertyManager.restore()
    }
}
