package jetbrains.kant.generator

import java.io.File
import java.io.FileWriter
import jetbrains.kant.gtcommon.ImportManager

class KotlinSourceFile(public val pkg: String?) {
    public val importManager: ImportManager = ImportManager(pkg)
    private val body = StringBuilder("")

    public fun append(code: String): KotlinSourceFile {
        body.append(code)
        return this
    }

    override public fun toString(): String {
        val result = StringBuilder()
        if (pkg != null) {
            result.append("package " + pkg + "\n\n")
        }
        if (!importManager.empty()) {
            result.append(importManager.toString()).append("\n")
        }
        result.append(body.toString())
        return result.toString()
    }

    public fun dump(file: File) {
        file.getParentFile()!!.mkdirs()
        file.createNewFile()
        val writer = FileWriter(file)
        writer.write(toString())
        writer.close()
    }
}
