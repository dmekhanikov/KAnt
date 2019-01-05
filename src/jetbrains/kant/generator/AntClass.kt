package jetbrains.kant.generator

import jetbrains.kant.gtcommon.AntAttribute
import jetbrains.kant.gtcommon.constants.ANT_CLASS_PREFIX
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*

class AntClass(classLoader: ClassLoader, val className: String) {
    private val PRIMITIVE_TYPES = HashMap<String, String>()

    init {
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

    val attributes: List<AntAttribute>
    val nestedElements: List<AntAttribute>
    val nestedTypes: List<String>
    val isTask: Boolean
    val isTaskContainer: Boolean
    val isTextContainer: Boolean
    val isCondition: Boolean
    val hasRefId: Boolean
    val implementedInterfaces: List<String>

    init {
        val classObject = classLoader.loadClass(className)!!
        isTask = classObject.isSubclassOf(ANT_CLASS_PREFIX + "Task") &&
                ((classObject.modifiers and Modifier.ABSTRACT) == 0)
        implementedInterfaces = classObject.getImplementedInterfaces()
        isTaskContainer = implementedInterfaces.contains(ANT_CLASS_PREFIX + "TaskContainer")
        isTextContainer = classObject.methods.firstOrNull {
            val name = it.name
            val parameters = it.parameterTypes!!
            name == "addText" && parameters.size == 1 && parameters[0].name == "java.lang.String"
        } != null
        isCondition = implementedInterfaces.contains(ANT_CLASS_PREFIX + "taskdefs.condition.Condition")
        attributes = classObject.getElements { parseAttribute(it) }
        hasRefId = attributes.firstOrNull {
            (it.name == "refid") && (it.typeName == ANT_CLASS_PREFIX + "types.Reference")
        } != null
        if (!isTaskContainer) {
            nestedElements = classObject.getElements { parseNestedElement(it) }
            nestedTypes = classObject.getNestedTypes()
        } else {
            nestedElements = ArrayList()
            nestedTypes = ArrayList()
        }
    }

    private fun Class<out Any?>.getNestedTypes(): List<String> {
        val res = ArrayList<String>()
        for (method in methods) {
            val methodName = method.name!!
            val parameterTypes = method.parameterTypes!!
            if ((methodName == "add" || methodName == "addConfigured") &&
                    parameterTypes.size == 1 && parameterTypes[0].isAntClass()) {
                val typeName = parameterTypes[0].name
                if (!res.contains(typeName)) {
                    res.add(typeName)
                }
            }
        }
        return res
    }

    private fun Class<out Any?>.getElements(parseElement: (method: Method) -> AntAttribute?): List<AntAttribute> {
        val elements = ArrayList<AntAttribute>()
        val usedNames = HashSet<String>()
        for (method in methods) {
            val element = parseElement(method)
            if (element != null && !usedNames.contains(element.name)) {
                elements.add(element)
                usedNames.add(element.name)
            }
        }
        return elements
    }

    private fun parseAttribute(method: Method): AntAttribute? {
        val methodName = method.name!!
        if (method.isAntAttributeSetter()) {
            val attributeName = cutElementName(methodName, "set".length)
            if (isCondition && attributeName == "refid") {
                return null
            }
            val attributeType = method.parameterTypes!![0]
            var attributeTypeName = attributeType.name
            if (PRIMITIVE_TYPES.containsKey(attributeTypeName)) {
                attributeTypeName = PRIMITIVE_TYPES[attributeTypeName]!!
            }
            return AntAttribute(attributeName, attributeTypeName)
        }
        return null
    }

    private fun parseNestedElement(method: Method): AntAttribute? {
        val methodName = method.name!!
        val returnTypeName = method.returnType!!.name
        if (methodName.startsWith("create") && method.returnType!!.isAntClass()) {
            val elementName = cutElementName(methodName, "create".length)
            if (elementName == "") {
                return null
            }
            return AntAttribute(elementName, returnTypeName)
        }
        val parameterTypes = method.parameterTypes!!
        if (methodName.startsWith("add") &&
                parameterTypes.size == 1 && parameterTypes[0].isAntClass()) {
            val elementType = method.parameterTypes!![0].name
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
            return AntAttribute(elementName, elementType)
        }
        return null
    }

    private fun Class<out Any?>.isAntClass(): Boolean {
        val name = name
        return !name.startsWith('[') && !name.startsWith("java.")
    }

    private fun Method.isAntAttributeSetter(): Boolean {
        val methodName = name!!
        return methodName.startsWith("set") && !parameterTypes!!.isEmpty() && parameterTypes!![0].isAntAttribute() &&
                methodName != "setTaskName" && methodName != "setTaskType" &&
                methodName != "setDescription"
    }

    private fun Class<out Any?>.isAntAttribute(): Boolean {
        val className = name
        return PRIMITIVE_TYPES.containsKey(className) || hasStringConstructor() ||
                className == ANT_CLASS_PREFIX + "types.Path" ||
                className == "java.lang.Class" ||
                isSubclassOf(ANT_CLASS_PREFIX + "types.EnumeratedAttribute") ||
                isEnum
    }

    private fun Class<out Any?>.hasStringConstructor(): Boolean {
        for (constructor in constructors) {
            val parameters = constructor.parameterTypes!!
            if (parameters.size == 1 && parameters[0].name == "java.lang.String") {
                return true
            }
        }
        return false
    }

    private fun Class<out Any?>.isSubclassOf(className: String): Boolean {
        var superclass = this as Class<*>?
        while (superclass != null) {
            if (superclass.name == className) {
                return true
            }
            superclass = superclass.superclass
        }
        return false
    }

    private fun getSuperclasses(): List<String> {
        return ArrayList()
    }

    private fun Class<out Any?>.getImplementedInterfaces(): List<String> {
        val candidates = ArrayList<Class<out Any?>>()
        val res = HashSet<String>()
        for (int in interfaces) {
            res.add(int.name)
            candidates.add(int)
        }
        val parent = (this as Class<*>).superclass
        if (parent != null) {
            candidates.add(parent)
        }
        for (candidate in candidates) {
            res.addAll(candidate.getImplementedInterfaces())
        }
        return res.toList()
    }

    private fun cutElementName(methodName: String, prefLen: Int): String {
        if (methodName.length <= prefLen) {
            return ""
        }
        return Character.toLowerCase(methodName[prefLen]) + methodName.substring(prefLen + 1)
    }
}
