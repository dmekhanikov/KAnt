package jetbrains.kant.dsl

import org.apache.tools.ant.Project
import org.apache.tools.ant.PropertyHelper
import java.io.File
import java.util.HashMap

private val defaultValues = HashMap<String, () -> Any?>()
private var propertyHelper: PropertyHelper? = null

fun initProperties(projectAO: Project) {
    propertyHelper = PropertyHelper.getPropertyHelper(projectAO)
    val basedir = File(".").getAbsoluteFile()!!.getParent().toString()
    propertyHelper!!.setUserProperty("basedir", basedir)
    for ((key, value) in defaultValues) {
        if (propertyHelper!!.getProperty(key) == null) {
            propertyHelper!!.setUserProperty(key, value())
        }
    }
}

public open class Property<T>(public val name: String? = null, public val convert: (value: String) -> T, public val defaultValue: (() -> T)?) {
    private fun getName(prop: PropertyMetadata): String {
        if (name != null) {
            return name!!
        } else {
            return prop.name
        }
    }

    public fun get(thisRef: Any?, prop: PropertyMetadata): T {
        val propName = getName(prop)
        var value = propertyHelper?.getProperty(propName)
        if (value == null) {
            if (defaultValue != null) {
                value = defaultValue!!()
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

    public fun set(thisRef: Any?, prop: PropertyMetadata, value: T) {
        val propName = getName(prop)
        if (propertyHelper!!.getProperty(propName) != null) {
            return
        }
        propertyHelper!!.setUserProperty(propName, value)
    }

    public fun propertyDelegated(prop: PropertyMetadata) {
        if (defaultValue != null && !defaultValues.contains(getName(prop))) {
            defaultValues[getName(prop)] = defaultValue!!
        }
    }
}

public fun propertyIsSet(name: String): Boolean {
    return propertyHelper!!.getProperty(name) != null
}

public class BooleanProperty(name: String? = null, defaultValue: (() -> Boolean)? = null) : Property<Boolean>(name, { java.lang.Boolean.parseBoolean(it) }, defaultValue)
public class CharProperty(name: String? = null, defaultValue: (() -> Char)? = null) : Property<Char>(name, { it[0] }, defaultValue)
public class ByteProperty(name: String? = null, defaultValue: (() -> Byte)? = null) : Property<Byte>(name, { java.lang.Byte.parseByte(it) }, defaultValue)
public class ShortProperty(name: String? = null, defaultValue: (() -> Short)? = null) : Property<Short>(name, { java.lang.Short.parseShort(it) }, defaultValue)
public class IntProperty(name: String? = null, defaultValue: (() -> Int)? = null) : Property<Int>(name, { java.lang.Integer.parseInt(it) }, defaultValue)
public class FloatProperty(name: String? = null, defaultValue: (() -> Float)? = null) : Property<Float>(name, { java.lang.Float.parseFloat(it) }, defaultValue)
public class LongProperty(name: String? = null, defaultValue: (() -> Long)? = null) : Property<Long>(name, { java.lang.Long.parseLong(it) }, defaultValue)
public class DoubleProperty(name: String? = null, defaultValue: (() -> Double)? = null) : Property<Double>(name, { java.lang.Double.parseDouble(it) }, defaultValue)
public class StringProperty(name: String? = null, defaultValue: (() -> String)? = null) : Property<String>(name, { it }, defaultValue)

public fun getProperty(name: String): Any {
    val value = propertyHelper!!.getProperty(name)
    if (value != null) {
        return value
    } else {
        throw IllegalStateException("Property $name has not been initialized")
    }
}

public fun <T>getProperty(name: String, convert: (String) -> T): T {
    val value = getProperty(name)
    if (value is String) {
        return convert(value)
    } else {
        return value as T
    }
}

public fun getBooleanProperty(name: String): Boolean { return getProperty(name, { java.lang.Boolean.parseBoolean(it) }) }
public fun getCharProperty(name: String): Char { return getProperty(name, { it[0] }) }
public fun getByteProperty(name: String): Byte { return getProperty(name, { java.lang.Byte.parseByte(it) }) }
public fun getShortProperty(name: String): Short { return getProperty(name, { java.lang.Short.parseShort(it) }) }
public fun getIntProperty(name: String): Int { return getProperty(name, { java.lang.Integer.parseInt(it) }) }
public fun getFloatProperty(name: String): Float { return getProperty(name, { java.lang.Float.parseFloat(it) }) }
public fun getLongProperty(name: String): Long { return getProperty(name, { java.lang.Long.parseLong(it) }) }
public fun getDoubleProperty(name: String): Double { return getProperty(name, { java.lang.Double.parseDouble(it) }) }
public fun getStringProperty(name: String): String { return getProperty(name, { it }) }
