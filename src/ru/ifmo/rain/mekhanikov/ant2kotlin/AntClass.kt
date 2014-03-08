package ru.ifmo.rain.mekhanikov.ant2kotlin

import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class AntClass(classLoader : ClassLoader, className : String) {
    private val PRIMITIVE_TYPES = HashMap<String, String>();
    {
        PRIMITIVE_TYPES["boolean"] = "Boolean"
        PRIMITIVE_TYPES["char"] = "Char"
        PRIMITIVE_TYPES["byte"] = "Byte"
        PRIMITIVE_TYPES["short"] = "Short"
        PRIMITIVE_TYPES["int"] = "Int"
        PRIMITIVE_TYPES["java.lang.Integer"] = "Int"
        PRIMITIVE_TYPES["float"] = "Float"
        PRIMITIVE_TYPES["long"] = "Long"
        PRIMITIVE_TYPES["double"] = "Double"
    }

    public val className : String = className
    public val attributes : List<AntClassAttribute>
    public val isTask : Boolean;
    {
        val classObject = classLoader.loadClass(className)!!
        val attributesArrayList = ArrayList<AntClassAttribute>()
        val usedAttributeNames = HashSet<String>()
        for (method in classObject.getMethods()) {
            val attribute = parseAttribute(method)
            if (attribute != null && !usedAttributeNames.contains(attribute.name)) {
                attributesArrayList.add(attribute)
                usedAttributeNames.add(attribute.name)
            }
        }
        attributes = attributesArrayList.toList()

        isTask = classObject.isSubclassOf("org.apache.tools.ant.Task") &&
                    ((classObject.getModifiers() and Modifier.ABSTRACT) == 0)
    }

    public fun toKotlin(pkg : String?): KotlinSourceFile {
        val res = KotlinSourceFile(pkg)
        val shortName = className.substring(className.lastIndexOf('.') + 1).replace("$", "")
        val tag = shortName.toLowerCase()
        res.append("class DSL$shortName : ${res.importManager.shorten("ru.ifmo.rain.mekhanikov.antdsl.DSLElement")}(\"$tag\") {\n")

        for (attr in attributes) {
            res.append("    var `${attr.name}` : ${res.importManager.shorten(attr.typeName.replace('$', '.'))} " +
            "by ${res.importManager.shorten("kotlin.properties.Delegates")}.mapVar(attributes)\n")
            res.dependencies.add(attr.typeName)
        }
        res.append("}\n")

        if (isTask) {
            res.append("\n")
            res.append("fun ${res.importManager.shorten("ru.ifmo.rain.mekhanikov.antdsl.DSLTarget")}"
                        + ".$tag(init: DSL$shortName.() -> ${res.importManager.shorten("jet.Unit")}): DSL$shortName =\n")
            res.append("        initElement(DSL$shortName(), init)\n")
        }

        return res
    }

    private fun Class<out Any?>.isSubclassOf(className : String): Boolean {
        [suppress("UNCHECKED_CAST")]
        var superclass : Class<Any?>? = this as Class<Any?>
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

    private fun Method.isAntAttributeSetter(): Boolean {
        val methodName = getName()!!
        return methodName.startsWith("set") && !getParameterTypes()!!.isEmpty() && getParameterTypes()!![0].isAntAttribute() &&
                !methodName.equals("setTaskName") && !methodName.equals("setTaskType") &&
                !methodName.equals("setLocation") && !methodName.equals("setDescription")
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
        if (method.isAntAttributeSetter()) {
            val attributeType = method.getParameterTypes()!![0]
            val attributeName = cutAttributeName(methodName)
            var attributeTypeName = attributeType.getName()
            if (PRIMITIVE_TYPES.containsKey(attributeTypeName)) {
                attributeTypeName = PRIMITIVE_TYPES[attributeTypeName]!!
            }
            return AntClassAttribute(attributeName, attributeTypeName, methodName)
        }
        return null
    }

    private fun cutAttributeName(name : String): String {
        val SETTER_PREFIX_LEN = 3;
        return name.substring(SETTER_PREFIX_LEN).toLowerCase()
    }
}

class AntClassAttribute(name : String, typeName : String, setterName : String) {
    public val name : String = name
    public val typeName : String = typeName
}