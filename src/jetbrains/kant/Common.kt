package jetbrains.kant

import java.io.File
import java.util.ArrayList
import java.net.URL
import java.net.URLClassLoader
import org.jetbrains.jet.cli.jvm.K2JVMCompiler
import java.util.regex.Pattern
import java.io.FileOutputStream
import java.util.jar.JarOutputStream
import java.util.HashSet

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
    compiler.exec(System.out, "-src", src, "-classpath", classpath, "-output", output)
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
