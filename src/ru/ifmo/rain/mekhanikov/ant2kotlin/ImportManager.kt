package ru.ifmo.rain.mekhanikov.ant2kotlin

import java.util.TreeMap
import java.util.regex.Pattern
import java.util.ArrayList
import ru.ifmo.rain.mekhanikov.explodeTypeName

class ImportManager {
    public val imports: TreeMap<String, String> = TreeMap() // short name -> full name

    private fun cutName(name: String): String? {
        val pos = name.lastIndexOf('.')
        if (pos != -1) {
            return name.substring(pos + 1)
        } else {
            return null
        }
    }

    public fun shorten(name: String): String {
        val names = explodeTypeName(name)
        val result = StringBuilder()
        for (noGenericName in names) {
            val cutName = cutName(noGenericName)
            val shorten = if (cutName != null && (!imports.containsKey(cutName) || imports[cutName] == noGenericName)) {
                imports[cutName] = noGenericName
                cutName
            } else {
                noGenericName
            }
            if (result.length() != 0) {
                result.append('<')
            }
            result.append(shorten)
        }
        for (i in 1..names.size - 1) {
            result.append('>')
        }
        return result.toString()
    }

    override public fun toString(): String {
        val res = StringBuilder("")
        for (name in imports.values()) {
            if (!name.startsWith("java.lang.") &&
                !(name.startsWith("kotlin.") && name.lastIndexOf('.') == "kotlin.".length - 1)) {
                res.append("import ").append(name).append("\n")
            }
        }
        return res.toString()
    }
}
