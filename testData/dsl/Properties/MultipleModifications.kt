import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.property

import jetbrains.kant.test.PropertyManager

val propertyManager = PropertyManager()

var prop by StringProperty() { "aaa" }

object project : DSLProject() {
    {
        prop = "bbb"
        property(name = "prop", value = "ccc")
    }
}

fun init() {
    propertyManager.setProperty("prop", "OK")
}

fun box(): String {
    try {
        init()
        project.perform()
        return prop
    } finally {
        propertyManager.restore()
    }
}
