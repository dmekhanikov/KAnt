package ru.ifmo.rain.mekhanikov.ant2kotlin

import java.util.HashSet
import java.io.File
import java.util.jar.JarInputStream
import ru.ifmo.rain.mekhanikov.ant2kotlin.createClassLoader
import java.io.FileInputStream

fun main(args : Array<String>) {
    DSLGenerator("lib/ant-1.9.3.jar", "dsl/src").generate()
}

class DSLGenerator(jarPath: String, resultRoot : String) {
    private val ANT_CLASS_PREFIX = "org.apache.tools.ant."
    private val DSL_PACKAGE = "ru.ifmo.rain.mekhanikov.antdsl"

    private val resolved = HashSet<String>()
    private val jarPath = jarPath
    private val classLoader = createClassLoader(jarPath)
    private val resultRoot = resultRoot + if (!resultRoot.endsWith('/')) {'/'} else {""}

    private fun resultClassName(srcClassName : String) : String {
        val className = srcClassName.replace("$", "")
        if (srcClassName.startsWith(ANT_CLASS_PREFIX)) {
            return DSL_PACKAGE + '.' + className.substring(ANT_CLASS_PREFIX.length)
        } else {
            return DSL_PACKAGE + ".other." + className
        }
    }

    private fun resultFile(srcClassName : String) : File {
        return File(resultRoot + resultClassName(srcClassName).replace('.', '/') + ".kt")
    }

    public fun resolveClass(className : String) {
        if (!className.startsWith(ANT_CLASS_PREFIX) || resolved.contains(className)) {
            return
        }
        val antClass = AntClass(classLoader, className)
        resolveClass(antClass)
    }

    public fun resolveClass(antClass : AntClass) {
        if (resolved.contains(antClass.className)) {
            return
        }
        resolved.add(antClass.className)
        val resultClassName = resultClassName(antClass.className)
        val kotlinSrcFile = antClass.toKotlin(resultClassName.substring(0, resultClassName.lastIndexOf('.')))
        kotlinSrcFile.dump(resultFile(antClass.className))
        for (dependency in kotlinSrcFile.dependencies) {
            resolveClass(dependency)
        }
    }

    private fun JarInputStream.nextAntTask() : AntClass? {
        var jarEntry = getNextJarEntry()
        while (jarEntry != null) {
            val entryName = jarEntry!!.getName()
            if (entryName.startsWith("org/apache/tools/ant/taskdefs/") &&
                entryName.endsWith(".class") &&
                !entryName.contains("$")) {
                val className = entryName.substring(0, entryName.lastIndexOf('.')).replace('/', '.')
                val antClass = AntClass(classLoader, className)
                if (antClass.isTask) {
                    return antClass
                }
            }
            jarEntry = getNextJarEntry()
        }
        return null
    }

    public fun generate() {
        File(resultRoot).mkdirs()
        val jis = JarInputStream(FileInputStream(jarPath))
        var antTaskClass = jis.nextAntTask()
        while (antTaskClass != null) {
            resolveClass(antTaskClass!!)
            antTaskClass = jis.nextAntTask()
        }
        jis.close()
    }
}
