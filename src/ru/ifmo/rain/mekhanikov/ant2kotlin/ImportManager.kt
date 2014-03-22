package ru.ifmo.rain.mekhanikov.ant2kotlin

import java.util.TreeMap

class ImportManager {
    public val imports : TreeMap<String, String> = TreeMap() // short name -> full name

    private fun cutName(name : String): String? {
        val pos = name.lastIndexOf('.')
        if (pos != -1) {
            return name.substring(pos + 1)
        } else {
            return null
        }
    }

    public fun shorten(name : String): String {
        val res = cutName(name)
        if (res != null && (!imports.containsKey(res) || imports[res] == name)) {
            imports[res] = name
            return res
        } else {
            return name
        }
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
