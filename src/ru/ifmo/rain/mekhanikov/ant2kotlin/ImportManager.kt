package ru.ifmo.rain.mekhanikov.ant2kotlin

import java.util.HashMap

class ImportManager {
    private val imports = HashMap<String, String>()

    private fun cutName(name : String): String {
        val pos = name.lastIndexOf('.')
        if (pos != -1) {
            return name.substring(pos + 1)
        } else {
            return name
        }
    }

    public fun shorten(name : String): String {
        val res = cutName(name)
        if (!imports.containsKey(res) || imports[res] == name) {
            imports[res] = name
            return res
        } else {
            return name
        }
    }

    public fun toString(): String {
        val res = StringBuilder("")
        for (name in imports.values()) {
            if (!name.startsWith("java.lang.")) {
                res.append("import ").append(name).append("\n")
            }
        }
        return res.toString()
    }
}
