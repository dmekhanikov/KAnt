package ru.ifmo.rain.mekhanikov.antdsl

import java.util.HashMap
import java.util.regex.Pattern
import java.io.File

val properties = HashMap<String, Any>()

fun initProperties(args: Array<String>) {
    properties.clear()
    for (arg in args) {
        val pattern = Pattern.compile("-D(\\w+)=(.*)")!!
        val matcher = pattern.matcher(arg)!!
        if (matcher.matches()) {
            val propName = matcher.group(1)!!
            val propVal = matcher.group(2)!!
            properties[propName] = propVal
        }
    }
}

open class Property<T>(val convert: (value: String) -> T, val defaultValue: () -> T) {
    public fun get(thisRef: Any?, prop: PropertyMetadata): T {
        if (properties.containsKey(prop.name)) {
            val value = properties[prop.name]
            return if (value is String) {
                convert(value)
            } else {
                value as T
            }
        } else {
            return defaultValue()
        }
    }

    public fun set(thisRef: Any?, prop: PropertyMetadata, value: T) {
        properties[prop.name] = value
    }
}

class BooleanProperty(defaultValue: Boolean) : Property<Boolean>({ java.lang.Boolean.parseBoolean(it) }, { defaultValue })
class CharProperty(defaultValue: Char) : Property<Char>({ it[0] }, { defaultValue })
class ByteProperty(defaultValue: Byte) : Property<Byte>({ java.lang.Byte.parseByte(it) }, { defaultValue })
class ShortProperty(defaultValue: Short) : Property<Short>({ java.lang.Short.parseShort(it) }, { defaultValue })
class IntProperty(defaultValue: Int) : Property<Int>({ Integer.parseInt(it) }, { defaultValue })
class FloatProperty(defaultValue: Float) : Property<Float>({ java.lang.Float.parseFloat(it) }, { defaultValue })
class LongProperty(defaultValue: Long) : Property<Long>({ java.lang.Long.parseLong(it) }, { defaultValue })
class DoubleProperty(defaultValue: Double) : Property<Double>({ java.lang.Double.parseDouble(it) }, { defaultValue })
class StringProperty(defaultValue: String) : Property<String>({ it }, { defaultValue })