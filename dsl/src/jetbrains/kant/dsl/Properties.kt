package jetbrains.kant.dsl

import org.apache.tools.ant.Project
import org.apache.tools.ant.PropertyHelper
import java.io.File
import java.util.HashMap
import kotlin.reflect.KProperty

private val defaultValues = HashMap<String, () -> Any?>()
var propertyHelper: PropertyHelper? = null

fun initProperties(projectAO: Project) {
    propertyHelper = PropertyHelper.getPropertyHelper(projectAO)
    val basedir = File(".").absoluteFile!!.parent.toString()
    propertyHelper!!.setUserProperty("basedir", basedir)
    for ((key, value) in defaultValues) {
        if (propertyHelper!!.getProperty(key) == null) {
            propertyHelper!!.setUserProperty(key, value())
        }
    }
}

open class Property<T>(val name: String? = null, val convert: (value: String) -> T, val defaultValue: (() -> T)?) {
    private fun getName(prop: KProperty<*>): String {
        return name ?: prop.name
    }

    fun get(thisRef: Any?, prop: KProperty<*>): T {
        val propName = getName(prop)
        var value = propertyHelper?.getProperty(propName)
        if (value == null) {
            if (defaultValue != null) {
                value = defaultValue.invoke()
            } else {
                throw IllegalStateException("Property ${getName(prop)} has not been initialized")
            }
        }
        val result = value
        return if (result is String) {
            convert(result)
        } else {
            result as T
        }
    }

    fun set(thisRef: Any?, prop: KProperty<*>, value: T) {
        val propName = getName(prop)
        if (propertyHelper!!.getProperty(propName) != null) {
            return
        }
        propertyHelper!!.setUserProperty(propName, value)
    }

    fun propertyDelegated(prop: KProperty<*>) {
        if (defaultValue != null && !defaultValues.contains(getName(prop))) {
            defaultValues[getName(prop)] = defaultValue
        }
    }
}

fun propertyIsSet(name: String): Boolean {
    return propertyHelper!!.getProperty(name) != null
}

class BooleanProperty(name: String? = null, defaultValue: (() -> Boolean)? = null) : Property<Boolean>(name, { java.lang.Boolean.parseBoolean(it) }, defaultValue)
class CharProperty(name: String? = null, defaultValue: (() -> Char)? = null) : Property<Char>(name, { it[0] }, defaultValue)
class ByteProperty(name: String? = null, defaultValue: (() -> Byte)? = null) : Property<Byte>(name, { java.lang.Byte.parseByte(it) }, defaultValue)
class ShortProperty(name: String? = null, defaultValue: (() -> Short)? = null) : Property<Short>(name, { java.lang.Short.parseShort(it) }, defaultValue)
class IntProperty(name: String? = null, defaultValue: (() -> Int)? = null) : Property<Int>(name, { java.lang.Integer.parseInt(it) }, defaultValue)
class FloatProperty(name: String? = null, defaultValue: (() -> Float)? = null) : Property<Float>(name, { java.lang.Float.parseFloat(it) }, defaultValue)
class LongProperty(name: String? = null, defaultValue: (() -> Long)? = null) : Property<Long>(name, { java.lang.Long.parseLong(it) }, defaultValue)
class DoubleProperty(name: String? = null, defaultValue: (() -> Double)? = null) : Property<Double>(name, { java.lang.Double.parseDouble(it) }, defaultValue)
class StringProperty(name: String? = null, defaultValue: (() -> String)? = null) : Property<String>(name, { it }, defaultValue)

fun getProperty(name: String): Any {
    val value = propertyHelper!!.getProperty(name)
    if (value != null) {
        return value
    } else {
        throw IllegalStateException("Property $name has not been initialized")
    }
}

fun <T> getProperty(name: String, convert: (String) -> T): T {
    val value = getProperty(name)
    if (value is String) {
        return convert(value)
    } else {
        return value as T
    }
}

fun getBooleanProperty(name: String): Boolean {
    return getProperty(name) { java.lang.Boolean.parseBoolean(it) }
}

fun getCharProperty(name: String): Char {
    return getProperty(name) { it[0] }
}

fun getByteProperty(name: String): Byte {
    return getProperty(name) { java.lang.Byte.parseByte(it) }
}

fun getShortProperty(name: String): Short {
    return getProperty(name) { java.lang.Short.parseShort(it) }
}

fun getIntProperty(name: String): Int {
    return getProperty(name) { java.lang.Integer.parseInt(it) }
}

fun getFloatProperty(name: String): Float {
    return getProperty(name) { java.lang.Float.parseFloat(it) }
}

fun getLongProperty(name: String): Long {
    return getProperty(name) { java.lang.Long.parseLong(it) }
}

fun getDoubleProperty(name: String): Double {
    return getProperty(name) { java.lang.Double.parseDouble(it) }
}

fun getStringProperty(name: String): String {
    return getProperty(name) { it }
}
