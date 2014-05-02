package ru.ifmo.rain.mekhanikov.ant2kotlin

import java.util.HashMap
import java.io.File
import java.util.jar.JarInputStream
import java.io.FileInputStream
import ru.ifmo.rain.mekhanikov.createClassLoader
import ru.ifmo.rain.mekhanikov.explodeTypeName
import java.util.HashSet

val DSL_PACKAGE = "ru.ifmo.rain.mekhanikov.antdsl"

class DSLGenerator(jarPath: String, resultRoot: String) {
    private val resolved = HashMap<String, AntClass>()
    private val jarPath = jarPath
    private val classLoader = createClassLoader(jarPath)
    private val resultRoot = resultRoot + if (resultRoot.endsWith('/')) {""} else {'/'}

    private val PRIMITIVE_TYPES = array("Boolean", "Char", "Byte", "Short", "Int", "Float", "Long", "Double").toSet()

    public fun resolveClass(className: String) {
        val names = explodeTypeName(className)
        for (name in names) {
            if (!name.startsWith(ANT_CLASS_PREFIX) || resolved.containsKey(name)) {
                return
            }
            val antClass = AntClass(classLoader, name)
            resolveClass(antClass)
        }
    }

    public fun resolveClass(antClass: AntClass) {
        if (resolved.containsKey(antClass.className)) {
            return
        }
        resolved[antClass.className] = antClass
        val resultClassName = resultClassName(antClass.className)
        val kotlinSrcFile = antClass.toKotlin(resultClassName.substring(0, resultClassName.lastIndexOf('.')))
        kotlinSrcFile.dump(resultFile(antClass.className))
    }

    public fun generate() {
        val generatedRoot = resultRoot + DSL_PACKAGE.replace('.', '/') + "/generated"
        File(generatedRoot).mkdirs()
        val jis = JarInputStream(FileInputStream(jarPath))
        var antTaskClass = jis.nextAntTask()
        while (antTaskClass != null) {
            resolveClass(antTaskClass!!)
            antTaskClass = jis.nextAntTask()
        }
        jis.close()
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

    private fun JarInputStream.nextAntTask(): AntClass? {
        var jarEntry = getNextJarEntry()
        while (jarEntry != null) {
            val entryName = jarEntry!!.getName()
            if (entryName.startsWith(ANT_CLASS_PREFIX.replace('.', '/') + "taskdefs/") &&
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
        val tag = cutTag(className)
        val dslElementShorten = res.importManager.shorten(DSL_PACKAGE + ".DSLElement")
        val dslTaskContainerShorten = res.importManager.shorten(DSL_PACKAGE + ".DSLTaskContainer")
        val dslProjectShorten = res.importManager.shorten(DSL_PACKAGE + ".DSLProject")
        res.append("class $dslTypeName() : ${if (isTaskContainer) {dslTaskContainerShorten} else {dslElementShorten}}(\"$tag\") {\n")
        for (attr in attributes) {
            val dslAttr = dslAttribute(attr, className)
            res.append("    var `${dslAttr.name}`: ${res.importManager.shorten(dslAttr.typeName.replace('$', '.'))} " +
                       "by ${res.importManager.shorten("kotlin.properties.Delegates")}.mapVar(attributes)\n")
        }
        res.append("}\n")
        for (element in nestedElements) {
            renderNestedElement(res, dslTypeName, element)
            resolveClass(element.typeName)
        }
        if (isTask) {
            renderNestedElement(res, dslTaskContainerShorten, Attribute(tag, className))
        }
        if (hasRefId) {
            renderNestedElement(res, dslProjectShorten, Attribute(tag, className))
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
        val elementClass = resolved[explodeTypeName(element.typeName)[0]]!!
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
                out.append("    return ${out.importManager.shorten(returnType)}(initElement(dslObject, init))\n")
            } else {
                out.append("    initElement(dslObject, init)\n")
            }
        } else {
            out.append("    return `$tag`(\n")
            for (attr in attributes) {
                out.append("        `${attr.name}`,\n")
            }
            out.append("        {})\n")
        }
        out.append("}\n")
    }

    private fun cutShortName(name: String): String {
        return name.substring(name.lastIndexOf('.') + 1).replace("$", "")
    }

    private fun cutTag(name: String): String {
        return cutShortName(name).toLowerCase()
    }
}