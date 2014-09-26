package jetbrains.kant.gtcommon

import java.util.HashSet
import java.util.HashMap

class ImportManager(val pkg: String?) {
    private val MIN_FOR_WILDCARD = 5
    private val fullNames = HashMap<String, String>() // short name -> full name
    private val imports = java.util.HashMap<String, HashSet<String>>()
    private var empty = true

    private fun cutName(name: String): String? {
        val pos = name.lastIndexOf('.')
        if (pos != -1) {
            return name.substring(pos + 1)
        } else {
            return null
        }
    }

    private fun cutPackageName(name: String): String? {
        val cutName = cutName(name)
        if (cutName != null && cutName.length < name.length) {
            return name.substring(0, name.length - cutName.length - 1)
        } else {
            return null
        }
    }

    private fun mustBePrinted(packageName: String): Boolean {
        return packageName != "java.lang" && packageName != "kotlin" && packageName != pkg
    }

    public fun addImport(name: String?, packageName: String?) {
        if (name == null || packageName == null) {
            return
        }
        var imported = imports[packageName]
        if (imported == null) {
            imported = java.util.HashSet()
            imports[packageName] = imported!!
        }
        imported!!.add(name)
        if (mustBePrinted(packageName)) {
            empty = false
        }
    }

    public fun addImport(name: String?) {
        if (name == null) {
            return
        }
        val shortName = cutName(name)
        val packageName = cutPackageName(name)
        addImport(shortName, packageName)
    }

    public fun shorten(name: String): String {
        val names = explodeTypeName(name)
        val result = StringBuilder()
        for (noGenericName in names) {
            val cutName = cutName(noGenericName)
            val packageName = cutPackageName(noGenericName)
            val shorten = if (cutName != null && (!fullNames.containsKey(cutName) || fullNames[cutName] == noGenericName)) {
                fullNames[cutName] = noGenericName
                cutName
            } else {
                noGenericName
            }
            addImport(cutName, packageName)
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

    public fun empty(): Boolean {
        return empty
    }

    override public fun toString(): String {
        val res = StringBuilder("")
        for (packageName in imports.keySet()) {
            if (mustBePrinted(packageName)) {
                val imported = imports[packageName]!!
                if (imported.size() >= MIN_FOR_WILDCARD) {
                    res.append("import ").append(packageName).append(".*\n")
                } else {
                    for (className in imported) {
                        res.append("import ").append(packageName).append('.').append(className).append("\n")
                    }
                }
            }
        }
        return res.toString()
    }
}
