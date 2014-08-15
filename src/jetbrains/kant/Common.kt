package jetbrains.kant

import java.util.ArrayList
import java.util.HashSet
import java.net.URL
import java.net.URLClassLoader
import org.jetbrains.jet.cli.jvm.K2JVMCompiler
import java.util.regex.Pattern
import java.util.jar.JarOutputStream
import java.util.jar.*
import java.io.*
import java.util.HashMap

public val KOTLIN_RUNTIME_JAR_FILE: String = "lib/kotlin-runtime.jar"
public val ANT_JAR_FILE: String = "lib/ant-1.9.4.jar"
public val keywords: HashSet<String> = array(
        "package",
        "as",
        "type",
        "class",
        "this",
        "super",
        "val",
        "var",
        "fun",
        "for",
        "null",
        "true",
        "false",
        "is",
        "in",
        "throw",
        "return",
        "break",
        "continue",
        "object",
        "if",
        "try",
        "else",
        "while",
        "do",
        "when",
        "trait",
        "This"
).toHashSet()

public fun escapeKeywords(string: String): String {
    return if (keywords.contains(string)) {
        "`$string`"
    } else {
        string
    }
}

public fun toCamelCase(name: String): String {
    val stringBuilder = StringBuilder(name)
    val separators = ".-_ "
    for (c in separators) {
        val separator = c.toString()
        var j = stringBuilder.indexOf(separator)
        while (j != -1) {
            stringBuilder.deleteCharAt(j)
            if (stringBuilder.length() > j) {
                stringBuilder.setCharAt(j, Character.toUpperCase(stringBuilder.charAt(j)))
            }
            j = stringBuilder.indexOf(separator, j)
        }
    }
    return escapeKeywords(stringBuilder.toString())
}

public fun createClassLoader(jars: Array<String>): ClassLoader {
    val path = ArrayList<URL>()
    for (jar in jars) {
        if (jar.endsWith(".jar")) {
            path.add(URL("jar:file:" + jar + "!/"))
        } else {
            path.add(URL("file:" + jar))
        }
    }
    return URLClassLoader(path.toArray(array(path[0])))
}

public fun File.cleanDirectory() {
    if (!exists()) {
        return
    }
    val files = listFiles()
    for (file in files!!) {
        if (file.isDirectory()) {
            file.cleanDirectory()
        }
        file.delete()
    }
    delete()
}

public fun File.deleteRecursively() {
    cleanDirectory()
    delete()
}

public fun compileKotlinCode(src: String, classpath: String, output: String) {
    val compiler = K2JVMCompiler()
    compiler.exec(System.out, "-src", src, "-classpath", classpath + File.pathSeparator + KOTLIN_RUNTIME_JAR_FILE, "-output", output)
}

public fun copy(inputStream: InputStream, outputStream: OutputStream) {
    val BUFFER_SIZE = 1024
    val buffer = ByteArray(BUFFER_SIZE)
    val bufferedInputStream = BufferedInputStream(inputStream)
    var len: Int
    while (true) {
        len = bufferedInputStream.read(buffer)
        if (len == -1) {
            break
        }
        outputStream.write(buffer, 0, len)
    }
    bufferedInputStream.close()
}

private fun copyFilesRecursively(dir: File, jarOutputStream: JarOutputStream, prefLen: Int) {
    for (file in dir.listFiles()!!) {
        if (file.isDirectory()) {
            copyFilesRecursively(file, jarOutputStream, prefLen)
        } else {
            val fileName = file.getCanonicalPath().substring(prefLen).replace('\\', '/')
            val jarEntry = JarEntry(fileName)
            jarEntry.setTime(file.lastModified())
            jarOutputStream.putNextEntry(jarEntry)
            val fileInputStream = FileInputStream(file)
            copy(fileInputStream, jarOutputStream)
            jarOutputStream.closeEntry()
        }
    }
}

public fun createJar(jarFile: String, srcDir: String) {
    val manifest = Manifest()
    manifest.getMainAttributes()!!.put(Attributes.Name.MANIFEST_VERSION, "1.0")
    File(jarFile).getParentFile()!!.mkdirs()
    val jarOutputStream = JarOutputStream(FileOutputStream(jarFile), manifest)
    val srcDirFile = File(srcDir).getCanonicalFile()
    val srcDirCanonical = srcDirFile.getCanonicalPath()
    val prefLen = srcDirCanonical.length + if (srcDirCanonical.endsWith('/')) { 0 } else { 1 }
    copyFilesRecursively(srcDirFile, jarOutputStream, prefLen)
    jarOutputStream.close()
}

public fun explodeTypeName(name: String): List<String> {
    val result = ArrayList<String>()
    val pattern = Pattern.compile("([^<>]*)<(.*)>")
    var remaining = name
    var matcher = pattern.matcher(remaining)
    while (matcher.matches()) {
        result.add(matcher.group(1)!!)
        remaining = matcher.group(2)!!
        matcher = pattern.matcher(remaining)
    }
    result.add(remaining)
    return result.toList()
}

public fun <K, V> List<V>.valuesToMap(getKey: (V) -> K): Map<K, V> {
    val result = HashMap<K, V>()
    for (value in this) {
        val key = getKey(value)
        result[key] = value
    }
    return result
}
