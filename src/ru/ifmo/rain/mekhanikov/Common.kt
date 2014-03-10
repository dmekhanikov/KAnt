package ru.ifmo.rain.mekhanikov

import java.io.File
import java.util.ArrayList
import java.net.URL
import java.net.URLClassLoader
import org.jetbrains.jet.cli.jvm.K2JVMCompiler

fun createClassLoader(vararg jars : String): ClassLoader {
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

fun File.cleanDirectory() {
    if (!exists()) {
        return
    }
    val files = listFiles()
    for (i in 0..files!!.size - 1) {
        val file = files[i]
        if (file.isDirectory()) {
            file.cleanDirectory()
        }
        file.delete()
    }
    delete()
}

fun File.deleteRecursively() {
    cleanDirectory()
    delete()
}

fun compileKotlinCode(src : String, classpath : String, output : String) {
    val compiler = K2JVMCompiler()
    compiler.exec(System.out, "-src", src, "-classpath", classpath, "-output", output)
}