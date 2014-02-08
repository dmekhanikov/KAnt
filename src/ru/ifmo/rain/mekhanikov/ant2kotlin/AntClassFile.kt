package ru.ifmo.rain.mekhanikov.ant2kotlin

import java.io.InputStream
import java.util.ArrayList

import ru.ifmo.rain.mekhanikov.ant2kotlin.dsl.DSLElementTemplate
import ru.ifmo.rain.mekhanikov.ant2kotlin.dsl.DSLAttributesTemplate
import java.util.HashMap

class AntClassFile(inputStream : InputStream) : ClassFile(inputStream) {
    private fun cutAttributeName(name : String): String {
        val SETTER_PREFIX_LEN = 3;
        return Character.toLowerCase(name.charAt(SETTER_PREFIX_LEN)).toString() + name.substring(SETTER_PREFIX_LEN + 1)
    }

    private fun cutArgType(desc : String): String {
        return desc.substring(1, desc.indexOf(')'))
    }

    private fun cutResultType(desc : String): String {
        return desc.substring(desc.indexOf(')') + 1)
    }

    private val TYPE_NAMES = HashMap<String, String>();
    {
        TYPE_NAMES["Z"] = "Boolean"
        TYPE_NAMES["C"] = "Char"
        TYPE_NAMES["B"] = "Byte"
        TYPE_NAMES["S"] = "Short"
        TYPE_NAMES["I"] = "Int"
        TYPE_NAMES["F"] = "Float"
        TYPE_NAMES["J"] = "Long"
        TYPE_NAMES["D"] = "Double"
        TYPE_NAMES["Ljava/lang/String;"] = "String"
    }

    private fun attributes(): List<AntClassAttribute> {
        val res = ArrayList<AntClassAttribute>()
        for (method in publicMethods) {
            if (method.getName()!!.startsWith("set")) {
                val name = cutAttributeName(method.getName()!!)
                val desc = method.getDescriptor()!!
                val argType = cutArgType(desc)
                val resultType = cutResultType(desc)
                if (resultType.equals("V") && TYPE_NAMES.containsKey(argType)) {
                    res.add(AntClassAttribute(name, TYPE_NAMES[argType]!!))
                }
            }
        }
        return res
    }

    public fun isTaskContainer(): Boolean {
        return interfaces.contains("org/apache/tools/ant/TaskContainer")
    }

    public fun supportsText(): Boolean {
        for (method in publicMethods) {
            if (method.getName()!!.equals("addText") && method.getDescriptor().equals("(Ljava/lang/String;)V")) {
                return true
            }
        }
        return false
    }

    public fun toKotlin(): String {
        val splitName = name.split("/")
        val dslAttributes = DSLAttributesTemplate(attributes())
        return DSLElementTemplate(splitName[splitName.size - 1], dslAttributes)
    }
}

class AntClassAttribute(name : String, typeName : String) {
    val name : String = name
    val typeName : String = typeName
}
