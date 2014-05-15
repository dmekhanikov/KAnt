package ru.ifmo.rain.mekhanikov.ant2kotlin

import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.lang.reflect.Method
import java.lang.reflect.Modifier

val ANT_CLASS_PREFIX = "org.apache.tools.ant."

class AntClass(classLoader: ClassLoader, className: String) {
    private val PRIMITIVE_TYPES = HashMap<String, String>();
    {
        PRIMITIVE_TYPES["boolean"] = "Boolean"
        PRIMITIVE_TYPES["java.lang.Boolean"] = "Boolean"
        PRIMITIVE_TYPES["char"] = "Char"
        PRIMITIVE_TYPES["java.lang.Char"] = "Char"
        PRIMITIVE_TYPES["byte"] = "Byte"
        PRIMITIVE_TYPES["java.lang.Byte"] = "Byte"
        PRIMITIVE_TYPES["short"] = "Short"
        PRIMITIVE_TYPES["java.lang.Short"] = "Short"
        PRIMITIVE_TYPES["int"] = "Int"
        PRIMITIVE_TYPES["java.lang.Integer"] = "Int"
        PRIMITIVE_TYPES["float"] = "Float"
        PRIMITIVE_TYPES["java.lang.Float"] = "Float"
        PRIMITIVE_TYPES["long"] = "Long"
        PRIMITIVE_TYPES["java.lang.Long"] = "Long"
        PRIMITIVE_TYPES["double"] = "Double"
        PRIMITIVE_TYPES["java.lang.Double"] = "Double"
    }

    public val className: String = className
    public val attributes: List<Attribute>
    public val nestedElements: List<Attribute>
    public val nestedTypes: List<String>
    public val isTask: Boolean
    public val isTaskContainer: Boolean
    public val hasRefId: Boolean
    public val implementedInterfaces: List<String>;
    {
        val classObject = classLoader.loadClass(className)!!
        isTask = classObject.isSubclassOf(ANT_CLASS_PREFIX + "Task") &&
                    ((classObject.getModifiers() and Modifier.ABSTRACT) == 0)
        attributes = classObject.getElements({parseAttribute(it)})
        implementedInterfaces = classObject.getImplementedInterfaces();
        isTaskContainer = implementedInterfaces.contains(ANT_CLASS_PREFIX + "TaskContainer")
        if (!isTaskContainer) {
            nestedElements = classObject.getElements({parseNestedElement(it)})
            nestedTypes = classObject.getNestedTypes()
        } else {
            nestedElements = ArrayList<Attribute>()
            nestedTypes = ArrayList<String>()
        }
        hasRefId = attributes.firstOrNull({
            (it.name == "refid") && (it.typeName == ANT_CLASS_PREFIX + "types.Reference")
        }) != null
    }

    private fun Class<out Any?>.getNestedTypes(): List<String> {
        val res = ArrayList<String>()
        for (method in getMethods()) {
            val methodName = method.getName()!!
            val parameterTypes = method.getParameterTypes()!!
            if ((methodName == "add" || methodName == "addConfigured") &&
                    parameterTypes.size == 1 && parameterTypes[0].isAntClass()) {
                val typeName = parameterTypes[0].getName()
                if (!res.contains(typeName)) {
                    res.add(typeName)
                }
            }
        }
        return res
    }

    private fun Class<out Any?>.getElements(parseElement: (method: Method) -> Attribute?): List<Attribute> {
        val elements = ArrayList<Attribute>()
        val usedNames = HashSet<String>()
        for (method in getMethods()) {
            val element = parseElement(method)
            if (element != null && !usedNames.contains(element.name)) {
                elements.add(element)
                usedNames.add(element.name)
            }
        }
        return elements
    }

    private fun parseAttribute(method: Method): Attribute? {
        val methodName = method.getName()!!
        if (method.isAntAttributeSetter()) {
            val attributeName = cutElementName(methodName, "set".length)
            val attributeType = method.getParameterTypes()!![0]
            var attributeTypeName = attributeType.getName()
            if (PRIMITIVE_TYPES.containsKey(attributeTypeName)) {
                attributeTypeName = PRIMITIVE_TYPES[attributeTypeName]!!
            }
            return Attribute(attributeName, attributeTypeName)
        }
        return null
    }

    private fun parseNestedElement(method: Method): Attribute? {
        val methodName = method.getName()!!
        val returnTypeName = method.getReturnType()!!.getName()
        if (methodName.startsWith("create") && method.getReturnType()!!.isAntClass()) {
            val elementName = cutElementName(methodName, "create".length)
            if (elementName == "") {
                return null
            }
            val elementType = returnTypeName
            return Attribute(elementName, elementType)
        }
        val parameterTypes = method.getParameterTypes()!!
        if (methodName.startsWith("add") &&
                parameterTypes.size == 1 && parameterTypes[0].isAntClass()) {
            val elementType = method.getParameterTypes()!![0].getName()
            val prefLen =
                    if (methodName.startsWith("addConfigured")) {
                        "addConfigured".length
                    } else {
                        "add".length
                    }
            if (methodName.length == prefLen) {
                return null
            }
            val elementName = cutElementName(methodName, prefLen)
            return Attribute(elementName, elementType)
        }
        return null
    }

    private fun Class<out Any?>.isAntClass(): Boolean {
        val name = getName()
        return !name.startsWith('[') && !name.startsWith("java.")
    }

    private fun Method.isAntAttributeSetter(): Boolean {
        val methodName = getName()!!
        return methodName.startsWith("set") && !getParameterTypes()!!.isEmpty() && getParameterTypes()!![0].isAntAttribute() &&
        methodName != "setTaskName" && methodName != "setTaskType" &&
        methodName != "setLocation" && methodName != "setDescription"
    }

    private fun Class<out Any?>.isAntAttribute(): Boolean {
        val className = getName()
        return PRIMITIVE_TYPES.containsKey(className) || hasStringConstructor() ||
        className == ANT_CLASS_PREFIX + "types.Path" ||
        className == "java.lang.Class" ||
        isSubclassOf(ANT_CLASS_PREFIX + "types.EnumeratedAttribute") ||
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

    private fun Class<out Any?>.isSubclassOf(className: String): Boolean {
        var superclass = this as Class<Any?>?
        while (superclass != null) {
            if (superclass!!.getName() == className) {
                return true
            }
            superclass = superclass!!.getSuperclass()
        }
        return false
    }

    private fun Class<out Any?>.getSuperclasses(): List<String> {
        val res = ArrayList<String>()

        return res
    }

    private fun Class<out Any?>.getImplementedInterfaces(): List<String> {
        val candidates = ArrayList<Class<out Any?>>()
        val res = HashSet<String>()
        for (interface in getInterfaces()) {
            res.add(interface.getName())
            candidates.add(interface)
        }
        val parent = (this as Class<Any?>).getSuperclass()
        if (parent != null) {
            candidates.add(parent)
        }
        for (candidate in candidates) {
            res.addAll(candidate.getImplementedInterfaces())
        }
        return res.toList()
    }

    private fun cutElementName(methodName: String, prefLen: Int): String {
        return methodName.substring(prefLen).toLowerCase()
    }
}

class Attribute(name: String, typeName: String) {
    public val name: String = name
    public val typeName: String = typeName
}
