package ru.ifmo.rain.mekhanikov.ant2kotlin

import java.io.File
import java.io.FileWriter
import java.util.HashSet

class KotlinSourceFile(pkg : String?) {
    public val importManager : ImportManager = ImportManager()
    public val dependencies : HashSet<String> = HashSet<String>()
    public var pkg : String? = pkg
    private val body = StringBuilder("")

    public fun append(code : String) {
        body.append(code)
    }

    public fun toString(): String {
        return (if (pkg != null) { "package " + pkg + "\n\n" } else { "" }) +
        importManager.toString() + "\n" + body.toString()
    }

    public fun dump(file : File) {
        file.getParentFile()!!.mkdirs()
        file.createNewFile()
        val writer = FileWriter(file)
        writer.write(toString())
        writer.close()
    }
}
