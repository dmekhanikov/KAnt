package ru.ifmo.rain.mekhanikov.ant2kotlin

import java.util.ArrayList
import java.util.HashMap
import java.net.URLClassLoader
import java.net.URL
import java.lang.reflect.Method

class AntClass(path : String, className : String) {

    private val PRIMITIVE_TYPES = HashMap<String, String>();
    {
        PRIMITIVE_TYPES["boolean"] = "Boolean"
        PRIMITIVE_TYPES["char"] = "Char"
        PRIMITIVE_TYPES["byte"] = "Byte"
        PRIMITIVE_TYPES["short"] = "Short"
        PRIMITIVE_TYPES["int"] = "Int"
        PRIMITIVE_TYPES["float"] = "Float"
        PRIMITIVE_TYPES["long"] = "Long"
        PRIMITIVE_TYPES["double"] = "Double"
    }

    private val classObject : Class<out Any?>
    public val attributes : List<AntClassAttribute>
    public val isTask : Boolean;
    {
        val jar = URL("file://" + path)
        val classLoader = URLClassLoader(Array<URL>(1){jar})
        classObject = classLoader.loadClass(className)!!
        val attributesArrayList = ArrayList<AntClassAttribute>()
        for (method in classObject.getMethods()) {
            val attribute = parseAttribute(method)
            if (attribute != null) {
                attributesArrayList.add(attribute)
            }
        }
        attributes = attributesArrayList.toList()

        isTask = classObject.isSubclassOf("org.apache.tools.ant.Task")
    }

    private fun Class<out Any?>.isSubclassOf(className : String): Boolean {
        var superclass : Class<Any?>? = classObject as Class<Any?>
        while (superclass != null) {
            if (superclass!!.getName().equals(className)) {
                return true
            }
            superclass = superclass!!.getSuperclass()
        }
        return false
    }

    private fun Class<out Any?>.isAntAttribute(): Boolean {
        val className = getName()
        return PRIMITIVE_TYPES.containsKey(className) || hasStringConstructor() ||
                className.equals("org.apache.tools.ant.types.Path") ||
                className.equals("java.lang.Class") ||
                isSubclassOf("org.apache.tools.ant.types.EnumeratedAttribute") ||
                isEnum()
    }

    private fun Class<out Any?>.hasStringConstructor(): Boolean {
        for (constructor in getConstructors()) {
            val parameters = constructor.getParameterTypes()!!
            if (parameters.size == 1 && parameters[0].getName() == "java.lang.String") {
                return true
            }
        }
        return false
    }

    private fun parseAttribute(method : Method) : AntClassAttribute? {
        val methodName = method.getName()!!
        if (methodName.startsWith("set")) {
            val attributeType = method.getParameterTypes()!![0]
            if (attributeType.isAntAttribute()) {
                val attributeName = cutAttributeName(methodName)
                var attributeTypeName = attributeType.getName()
                if (PRIMITIVE_TYPES.containsKey(attributeTypeName)) {
                    attributeTypeName = PRIMITIVE_TYPES[attributeTypeName]!!
                }
                return AntClassAttribute(attributeName, attributeTypeName)
            }
        }
        return null
    }

    private fun cutAttributeName(name : String): String {
        val SETTER_PREFIX_LEN = 3;
        return Character.toLowerCase(name.charAt(SETTER_PREFIX_LEN)).toString() + name.substring(SETTER_PREFIX_LEN + 1)
    }
}

class AntClassAttribute(name : String, typeName : String) {
    val name : String = name
    val typeName : String = typeName
}

class KotlinSourceFile {
    public val importManager : ImportManager = ImportManager()
    private val body = StringBuilder("")

    public fun append(code : String) {
        body.append(code)
    }

    public fun toString(): String {
        return importManager.toString() + "\n" + body.toString()
    }
}