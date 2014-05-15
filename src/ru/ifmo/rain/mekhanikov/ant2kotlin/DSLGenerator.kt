package ru.ifmo.rain.mekhanikov.ant2kotlin

import java.util.HashMap
import java.io.File
import java.util.jar.JarInputStream
import java.io.FileInputStream
import ru.ifmo.rain.mekhanikov.createClassLoader
import ru.ifmo.rain.mekhanikov.explodeTypeName
import java.util.regex.Pattern
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.HashSet

val DSL_PACKAGE = "ru.ifmo.rain.mekhanikov.antdsl"

class DSLGenerator(resultRoot: String, vararg val jarPath: String) {
    private val resolved = HashMap<String, Target>()
    private val containerGenerator = ContainerGenerator()
    private var classLoader: ClassLoader = createClassLoader(jarPath)
    private val resultRoot = resultRoot + if (resultRoot.endsWith('/')) {""} else {'/'}

    private var propertiesReader: BufferedReader? = null

    private val PRIMITIVE_TYPES = array("Boolean", "Char", "Byte", "Short", "Int", "Float", "Long", "Double").toSet()

    public fun resolveClass(className: String) {
        val names = explodeTypeName(className)
        for (name in names) {
            if (!resolved.containsKey(name)) {
                val antClass: AntClass?
                try {
                    antClass = AntClass(classLoader, className)
                } catch (e: ClassNotFoundException) {
                    antClass = null
                }
                if (antClass != null) {
                    val target = Target(className)
                    target.antClass = antClass
                    resolved[className] = target
                    val resultClassName = resultClassName(className)
                    val pkg = resultClassName.substring(0, resultClassName.lastIndexOf('.'))
                    target.kotlinSrcFile = antClass.toKotlin(pkg)
                    containerGenerator.resolveContainers(antClass.implementedInterfaces)
                    containerGenerator.resolveContainers(antClass.nestedTypes)
                }
            }
        }
    }

    public fun generate() {
        val generatedRoot = resultRoot + DSL_PACKAGE.replace('.', '/') + "/generated"
        File(generatedRoot).mkdirs()
        for (jar in jarPath) {
            val jis = JarInputStream(FileInputStream(jar))
            var alias = jis.nextAlias()
            while (alias != null) {
                val tag = alias!!.tag
                val className = alias!!.className
                resolveClass(className)
                resolved[className]?.generateConstructors(tag)
                alias = jis.nextAlias()
            }
            jis.close()
        }
        for (target in resolved.values()) {
            target.dumpFile()
        }
        containerGenerator.dump(File(generatedRoot + "/Containers.kt"))
    }

    private fun resultClassName(srcClassName: String): String {
        val className = srcClassName.replace("$", "")
        return DSL_PACKAGE + ".DSL" + className.substring(className.lastIndexOf('.') + 1)
    }

    private fun resultFile(srcClassName: String): File {
        val className = srcClassName.replace("$", "")
        val relativePath = StringBuilder()
        relativePath.append(DSL_PACKAGE + ".generated.")
        if (srcClassName.startsWith(ANT_CLASS_PREFIX)) {
            relativePath.append(className.substring(ANT_CLASS_PREFIX.length))
        } else {
            relativePath.append("other." + className)
        }
        relativePath.insert(relativePath.lastIndexOf(".") + 1, "DSL")
        return File(resultRoot + relativePath.toString().replace('.', '/') + ".kt")
    }

    private fun JarInputStream.nextAlias(): Alias? {
        var line = nextPropertiesLine()
        while (line != null) {
            val alias = parseAlias(line!!)
            if (alias != null) {
                return alias
            }
            line = nextPropertiesLine()
        }
        return null
    }

    private fun updatePropertiesReader(jis: JarInputStream): Boolean {
        val propertiesFileName = jis.nextPropertiesFileName()
        if (propertiesFileName == null) {
            return false
        }
        propertiesReader = BufferedReader(InputStreamReader(classLoader.getResourceAsStream(propertiesFileName)!!))
        return true
    }

    private fun JarInputStream.nextPropertiesLine(): String? {
        var line = propertiesReader?.readLine()
        while (line == null) {
            if (!updatePropertiesReader(this)) {
                return null
            }
            line = propertiesReader!!.readLine()
        }
        return line
    }

    private fun JarInputStream.nextPropertiesFileName(): String? {
        var jarEntry = getNextJarEntry()
        while (jarEntry != null) {
            val entryName = jarEntry!!.getName()
            if (entryName.endsWith(".properties")) {
                return entryName
            }
            jarEntry = getNextJarEntry()
        }
        return null
    }

    private fun parseAlias(line: String): Alias? {
        val pattern = Pattern.compile("^(\\w*)\\s*=\\s*([\\w.]*)$")
        val matcher = pattern.matcher(line)
        if (matcher.matches()) {
            val tag = matcher.group(1)!!
            val className = matcher.group(2)!!
            return Alias(tag, className)
        } else {
            return null
        }
    }

    private fun dslAttribute(attr: Attribute, parentName: String): Attribute {
        val name = attr.name
        val typeName =
                if (PRIMITIVE_TYPES.contains(attr.typeName)) {
                    attr.typeName
                } else if (attr.typeName == ANT_CLASS_PREFIX + "types.Reference") {
                    if (attr.name == "refid") {
                        DSL_PACKAGE + ".Reference<${resultClassName(parentName)}>"
                    } else if (attr.name.endsWith("pathref")) {
                        DSL_PACKAGE + ".Reference<$DSL_PACKAGE.DSLPath>"
                    } else if (attr.name == "loaderref") {
                         DSL_PACKAGE + ".LoaderRef"
                    } else {
                        "java.lang.String"
                    }
                } else {
                    "java.lang.String"
                }
        return Attribute(name, typeName)
    }

    private fun AntClass.toKotlin(pkg: String?): KotlinSourceFile {
        val res = KotlinSourceFile(pkg)
        val dslTypeName = "DSL" + cutShortName(className)
        val dslElementShorten = res.importManager.shorten(DSL_PACKAGE + ".DSLElement")
        val dslTaskContainerShorten = res.importManager.shorten(DSL_PACKAGE + ".DSLTaskContainer")
        res.append("class $dslTypeName : ${if (isTaskContainer) {dslTaskContainerShorten} else {dslElementShorten}}()")
        for (nestedType in nestedTypes) {
            val containerName = containerGenerator.containerName(nestedType)
            res.append(",\n        $containerName")
        }
        res.append(" {\n")
        for (attr in attributes) {
            val dslAttr = dslAttribute(attr, className)
            res.append("    var `${dslAttr.name}`: ${res.importManager.shorten(dslAttr.typeName.replace('$', '.'))} " +
                       "by ${res.importManager.shorten("kotlin.properties.Delegates")}.mapVar(attributes)\n")
        }
        res.append("}\n")
        for (element in nestedElements) {
            renderNestedElement(res, dslTypeName, element)
        }
        return res
    }

    private fun AntClass.constructorReturnType(): String? {
        val typeName = resultClassName(className)
        if (hasRefId) {
            return DSL_PACKAGE + ".Reference<$typeName>"
        } else {
            return null
        }
    }

    private fun AntClass.renderNestedElement(out: KotlinSourceFile,
                                             parentName: String, element: Attribute) {
        resolveClass(element.typeName)
        val elementClass = resolved[explodeTypeName(element.typeName)[0]]!!.antClass!!
        elementClass.renderConstructor(out, parentName, element.name, true)
        elementClass.renderConstructor(out, parentName, element.name, false)
    }

    private fun AntClass.renderConstructorParameters(out: KotlinSourceFile) {
        var first = true
        for (attr in attributes) {
            val dslAttr = dslAttribute(attr, className)
            if (first) {
                first = false
            } else {
                out.append(",\n")
            }
            out.append("        `${dslAttr.name}`: ${out.importManager.shorten(dslAttr.typeName.replace('$', '.'))}? = null")
        }
    }

    private fun AntClass.renderConstructor(out: KotlinSourceFile,
                                           parentName: String, tag: String, withInit: Boolean) {
        val dslTypeName = out.importManager.shorten(resultClassName(className))
        out.append("\n")
        out.append("fun $parentName.`$tag`(\n")
        renderConstructorParameters(out)
        if (withInit) {
            if (!attributes.empty) {
                out.append(",\n")
            }
            out.append("        init: $dslTypeName.() -> ${out.importManager.shorten("kotlin.Unit")}")
        }
        out.append(")")
        val returnType = constructorReturnType()
        if (returnType != null) {
            out.append(": " + out.importManager.shorten(returnType))
        }
        out.append(" {\n")
        if (withInit) {
            out.append("    val dslObject = $dslTypeName()\n")
            for (attr in attributes) {
                val dslAttr = dslAttribute(attr, className)
                out.append("    if (`${dslAttr.name}` != null) { dslObject.`${dslAttr.name}` = `${dslAttr.name}` }\n")
            }
            if (returnType != null) {
                out.append("    return ${out.importManager.shorten(returnType)}(initElement(\"$tag\", dslObject, init))\n")
            } else {
                out.append("    initElement(\"$tag\", dslObject, init)\n")
            }
        } else {
            out.append("    return `$tag`(\n")
            for (attr in attributes) {
                out.append("        `${attr.name}`,\n")
            }
            out.append("        {$dslTypeName.() -> })\n")
        }
        out.append("}\n")
    }

    private fun cutShortName(name: String): String {
        return name.substring(name.lastIndexOf('.') + 1).replace("$", "")
    }

    private inner class Target(val className: String) {
        var antClass: AntClass? = null
        var kotlinSrcFile: KotlinSourceFile? = null

        fun generateConstructors(tag: String) {
            val invAntClass = antClass!!
            val invKotlinSrcFile = kotlinSrcFile!!
            val dslTaskContainerShorten = invKotlinSrcFile.importManager.shorten(DSL_PACKAGE + ".DSLTaskContainer")
            val dslProjectShorten = invKotlinSrcFile.importManager.shorten(DSL_PACKAGE + ".DSLProject")
            if (invAntClass.isTask) {
                invAntClass.renderNestedElement(invKotlinSrcFile, dslTaskContainerShorten, Attribute(tag, className))
            }
            if (invAntClass.hasRefId) {
                invAntClass.renderNestedElement(invKotlinSrcFile, dslProjectShorten, Attribute(tag, className))
            }
            for (interface in invAntClass.implementedInterfaces) {
                if (containerGenerator.typeNeedsContainer(interface)) {
                    val containerName = containerGenerator.containerName(interface)
                    invAntClass.renderNestedElement(invKotlinSrcFile, containerName, Attribute(tag, className))
                }
            }
        }

        fun dumpFile() {
            kotlinSrcFile!!.dump(resultFile(className))
        }
    }

    private class Alias(val tag: String, val className: String)
}

class ContainerGenerator {
    private val kotlinSrcFile = KotlinSourceFile(DSL_PACKAGE)
    private val resolved = HashSet<String>()

    public fun resolveContainers(names: List<String>) {
        for (name in names) {
            resolveContainer(name)
        }
    }

    public fun containerName(name: String): String {
        val shortName = name.substring(name.lastIndexOf('.') + 1).replace("$", "")
        return shortName + "Container"
    }

    public fun typeNeedsContainer(name: String): Boolean {
        return !name.startsWith("java.")
    }

    public fun resolveContainer(name: String) {
        if (!resolved.contains(name) && typeNeedsContainer(name)) {
            val dslElementShorten = kotlinSrcFile.importManager.shorten(DSL_PACKAGE + ".DSLElement")
            kotlinSrcFile.append("\ntrait ${containerName(name)} : $dslElementShorten\n")
            resolved.add(name)
        }
    }

    public fun dump(file: File) {
        kotlinSrcFile.dump(file)
    }
}
