package ru.ifmo.rain.mekhanikov.ant2kotlin

import java.util.HashMap
import java.io.File
import java.util.jar.JarInputStream
import java.io.FileInputStream
import ru.ifmo.rain.mekhanikov.createClassLoader

class DSLGenerator(jarPath: String, resultRoot : String) {
    private val ANT_CLASS_PREFIX = "org.apache.tools.ant."
    private val DSL_PACKAGE = "ru.ifmo.rain.mekhanikov.antdsl"
    private val GENERATED_DSL_PACKAGE = DSL_PACKAGE + ".generated"

    private val resolved = HashMap<String, AntClass>()
    private val jarPath = jarPath
    private val classLoader = createClassLoader(jarPath)
    private val resultRoot = resultRoot + if (resultRoot.endsWith('/')) {""} else {'/'}

    public fun resolveClass(className : String) {
        if (!className.startsWith(ANT_CLASS_PREFIX) || resolved.containsKey(className)) {
            return
        }
        val antClass = AntClass(classLoader, className)
        resolveClass(antClass)
    }

    public fun resolveClass(antClass : AntClass) {
        if (resolved.containsKey(antClass.className)) {
            return
        }
        resolved[antClass.className] = antClass
        val resultClassName = resultClassName(antClass.className)
        val kotlinSrcFile = antClass.toKotlin(resultClassName.substring(0, resultClassName.lastIndexOf('.')))
        kotlinSrcFile.dump(resultFile(antClass.className))
    }

    public fun generate() {
        val generatedRoot = resultRoot + GENERATED_DSL_PACKAGE.replace('.', '/')
        File(generatedRoot).mkdirs()
        val jis = JarInputStream(FileInputStream(jarPath))
        var antTaskClass = jis.nextAntTask()
        while (antTaskClass != null) {
            resolveClass(antTaskClass!!)
            antTaskClass = jis.nextAntTask()
        }
        jis.close()
    }

    private fun resultClassName(srcClassName : String) : String {
        val className = srcClassName.replace("$", "")
        val result = StringBuilder()
        result.append(GENERATED_DSL_PACKAGE + ".")
        if (srcClassName.startsWith(ANT_CLASS_PREFIX)) {
            result.append(className.substring(ANT_CLASS_PREFIX.length))
        } else {
            result.append("other." + className)
        }
        result.insert(result.lastIndexOf(".") + 1, "DSL")
        return result.toString()
    }

    private fun resultFile(srcClassName : String) : File {
        return File(resultRoot + resultClassName(srcClassName).replace('.', '/') + ".kt")
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

    private fun AntClass.toKotlin(pkg : String?): KotlinSourceFile {
        val res = KotlinSourceFile(pkg)
        val dslTypeName = "DSL" + cutShortName(className)
        val tag = cutTag(className)
        val dslElementShorten = res.importManager.shorten("ru.ifmo.rain.mekhanikov.antdsl.DSLElement")
        val dslTaskContainerShorten = res.importManager.shorten("ru.ifmo.rain.mekhanikov.antdsl.DSLTaskContainer")
        res.append("class $dslTypeName() : ${if (isTaskContainer) {dslTaskContainerShorten} else {dslElementShorten}}(\"$tag\") {\n")
        for (attr in attributes) {
            res.append("    var `${attr.name}` : ${res.importManager.shorten(attr.typeName.replace('$', '.'))} " +
            "by ${res.importManager.shorten("kotlin.properties.Delegates")}.mapVar(attributes)\n")
            resolveClass(attr.typeName)
        }
        res.append("}\n")
        for (element in nestedElements) {
            renderNestedElement(res, dslTypeName, element)
            resolveClass(element.typeName)
        }
        if (isTask) {
            renderNestedElement(res, dslTaskContainerShorten,
                    AntClassElement(tag, className))
        }
        return res
    }

    private fun AntClass.renderNestedElement(out : KotlinSourceFile,
                                             parentName : String, element : AntClassElement) {
        val dslTypeName = out.importManager.shorten(resultClassName(element.typeName))
        val tag = element.name
        out.append("\n")
        val funName = "${parentName}.`$tag`"
        out.append("fun $funName(\n")
        if (!resolved.containsKey(element.typeName)) {
            resolveClass(element.typeName)
        }
        val elementClass = resolved[element.typeName]!!
        elementClass.renderConstructorParameters(out)
        if (!elementClass.attributes.empty) {
            out.append(",\n")
        }
        out.append("        init: $dslTypeName.() -> ${out.importManager.shorten("jet.Unit")}): $dslTypeName {\n")
        out.append("    val dslObject = $dslTypeName()\n")
        for (attr in elementClass.attributes) {
            out.append("    if (`${attr.name}` != null) { dslObject.`${attr.name}` = `${attr.name}` }\n")
        }
        out.append("    return initElement(dslObject, init)\n")
        out.append("}\n")

        out.append("\n")
        out.append("fun $funName(\n")
        elementClass.renderConstructorParameters(out)
        out.append("): $dslTypeName {\n")
        out.append("    return `$tag`(\n")
        for (attr in elementClass.attributes) {
            out.append("        `${attr.name}`,\n")
        }
        out.append("        {})\n")
        out.append("}\n")
    }

    private fun AntClass.renderConstructorParameters(out : KotlinSourceFile) {
        var first = true
        for (attr in attributes) {
            if (first) {
                first = false
            } else {
                out.append(",\n")
            }
            out.append("        `${attr.name}` : ${out.importManager.shorten(attr.typeName.replace('$', '.'))}? = null")
        }
    }

    private fun cutShortName(name : String) : String {
        return name.substring(name.lastIndexOf('.') + 1).replace("$", "")
    }

    private fun cutTag(name : String) : String {
        return cutShortName(name).toLowerCase()
    }
}