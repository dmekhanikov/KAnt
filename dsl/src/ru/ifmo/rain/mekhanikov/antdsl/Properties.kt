package ru.ifmo.rain.mekhanikov.antdsl

import java.util.regex.Pattern
import org.apache.tools.ant.Project
import org.apache.tools.ant.PropertyHelper

var propertyHelper: PropertyHelper? = null

fun initProperties(project: Project, args: Array<String>) {
    propertyHelper = PropertyHelper.getPropertyHelper(project)
    for (arg in args) {
        val pattern = Pattern.compile("-D(\\w+)=(.*)")!!
        val matcher = pattern.matcher(arg)!!
        if (matcher.matches()) {
            val propName = matcher.group(1)!!
            val propVal = matcher.group(2)!!
            propertyHelper!!.setUserProperty(propName, propVal)
        }
    }
}

open class Property<T>(val convert: (value: String) -> T, val defaultValue: () -> T, val name: String? = null) {
    private fun getName(prop: PropertyMetadata): String {
        if (name != null) {
            return name
        } else {
            return prop.name
        }
    }

    public fun get(thisRef: Any?, prop: PropertyMetadata): T {
        val propName = getName(prop)
        var value = propertyHelper!!.getProperty(propName)
        if (value == null) {
            value = defaultValue()
            propertyHelper!!.setUserProperty(propName, value)
        }
        val result = value
        return if (result is String) {
            convert(result)
        } else {
            value as T
        }
    }

    public fun set(thisRef: Any?, prop: PropertyMetadata, value: T) {
        val propName = getName(prop)
        propertyHelper!!.setUserProperty(propName, value)
    }

    public fun isSet(thisRef: Any?, prop: PropertyMetadata): Boolean {
        return propertyIsSet(getName(prop))
    }
}

public fun propertyIsSet(name: String): Boolean {
    return propertyHelper!!.getProperty(name) != null
}

class BooleanProperty(defaultValue: Boolean, name: String? = null) : Property<Boolean>({ java.lang.Boolean.parseBoolean(it) }, { defaultValue }, name)
class CharProperty(defaultValue: Char, name: String? = null) : Property<Char>({ it[0] }, { defaultValue }, name)
class ByteProperty(defaultValue: Byte, name: String? = null) : Property<Byte>({ java.lang.Byte.parseByte(it) }, { defaultValue }, name)
class ShortProperty(defaultValue: Short, name: String? = null) : Property<Short>({ java.lang.Short.parseShort(it) }, { defaultValue }, name)
class IntProperty(defaultValue: Int, name: String? = null) : Property<Int>({ Integer.parseInt(it) }, { defaultValue }, name)
class FloatProperty(defaultValue: Float, name: String? = null) : Property<Float>({ java.lang.Float.parseFloat(it) }, { defaultValue }, name)
class LongProperty(defaultValue: Long, name: String? = null) : Property<Long>({ java.lang.Long.parseLong(it) }, { defaultValue }, name)
class DoubleProperty(defaultValue: Double, name: String? = null) : Property<Double>({ java.lang.Double.parseDouble(it) }, { defaultValue }, name)
class StringProperty(defaultValue: String, name: String? = null) : Property<String>({ it }, { defaultValue }, name)
