package ru.ifmo.rain.mekhanikov.ant2kotlin

import ru.ifmo.rain.mekhanikov.*
import java.util.HashMap
import java.util.HashSet
import java.util.regex.Pattern
import java.util.jar.JarInputStream
import java.io.*
import java.io.File.pathSeparator
import org.kohsuke.args4j.*


public val DSL_ROOT: String = "dsl/src/"
val DSL_PACKAGE = "ru.ifmo.rain.mekhanikov.antdsl"
val BASE_DSL_FILE_NAMES = array("Base.kt", "Properties.kt", "References.kt", "Misc.kt", "LazyTask.kt", "Text.kt")

fun main(args: Array<String>) {
    GeneratorRunner().doMain(args)
}

class GeneratorRunner {
    [Option(name = "-cp", usage = "classpath")]
    private val classpath: String = ""

    [Option(name = "-o", usage = "output directory", required = true)]
    private val outDir: File? = null

    [Option(name = "--seek", usage = "seek alias files")]
    private val seek = false

    [Argument]
    private val aliasFiles: Array<String> = array()

    fun doMain(args: Array<String>) {
        val parser = CmdLineParser(this);
        parser.setUsageWidth(80);
        try {
            parser.parseArgument(args.toArrayList());
        } catch(e: CmdLineException) {
            System.err.println(e.getMessage());
            System.err.println("java Ant2KotlinPackage [options...] arguments...");
            parser.printUsage(System.err);
            System.err.println();
            return;
        }
        val classpathArray = classpath.split(pathSeparator)
        copyBaseFiles(outDir!!)
        DSLGenerator(outDir.toString(), classpathArray, aliasFiles, seek).generate()
    }
}

private fun copyBaseFiles(dest: File) {
    val srcPrefix = "$DSL_ROOT${DSL_PACKAGE.replace('.', '/')}/"
    val destPrefix = "$dest/${DSL_PACKAGE.replace('.', '/')}/"
    for (fileName in BASE_DSL_FILE_NAMES) {
        val srcFile = File(srcPrefix + fileName)
        val destFile = File(destPrefix + fileName)
        srcFile.copyTo(destFile)
    }
}

class DSLGenerator(resultRoot: String, val classpath: Array<String>, val aliasFiles: Array<String>, val seekForAliasFiles: Boolean = false) {
    private val resolved = HashMap<String, Target>()
    private val containerGenerator = ContainerGenerator()
    private var classLoader: ClassLoader = createClassLoader(classpath)
    private val resultRoot = resultRoot + if (resultRoot.endsWith('/')) {""} else {'/'}

    private var aliasReader: BufferedReader? = null

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
                    target.kotlinSrcFile = antClass.toKotlin(DSL_PACKAGE)
                    containerGenerator.resolveContainers(antClass.implementedInterfaces)
                    containerGenerator.resolveContainers(antClass.nestedTypes)
                }
            }
        }
    }

    public fun generate() {
        val generatedRoot = resultRoot + DSL_PACKAGE.replace('.', '/') + "/generated"
        File(generatedRoot).mkdirs()
        if (seekForAliasFiles) {
            for (jar in classpath) {
                if (!jar.endsWith(".jar")) {
                    continue
                }
                val jis = JarInputStream(FileInputStream(jar))
                var alias = jis.nextAlias()
                while (alias != null) {
                    resolveAlias(alias!!)
                    alias = jis.nextAlias()
                }
                jis.close()
            }
        }
        for (aliasFileName in aliasFiles) {
            var stream = classLoader.getResourceAsStream(aliasFileName)
            if (stream == null) {
                val aliasFile = File(aliasFileName)
                if (!aliasFile.exists()) {
                    System.err.println("File $aliasFile was not found")
                    continue
                }
                stream = FileInputStream(aliasFile)
            }
            val bf = BufferedReader(InputStreamReader(stream!!))
            var line = bf.readLine()
            while (line != null) {
                val alias = parseAlias(line!!)
                if (alias != null) {
                    resolveAlias(alias)
                }
                line = bf.readLine()
            }
        }
        for (target in resolved.values()) {
            target.dumpFile()
        }
        containerGenerator.dump(File(generatedRoot + "/Containers.kt"))
    }

    private fun resolveAlias(alias: Alias) {
        val tag = alias.tag
        val className = alias.className
        resolveClass(className)
        resolved[className]?.generateConstructors(tag)
    }

    private fun resultClassName(srcClassName: String): String {
        val className = srcClassName.replace("$", "")
        return "DSL" + className.substring(className.lastIndexOf('.') + 1)
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
        val aliasFileName = jis.nextPropertiesFileName()
        if (aliasFileName == null) {
            return false
        }
        aliasReader = BufferedReader(InputStreamReader(classLoader.getResourceAsStream(aliasFileName)!!))
        return true
    }

    private fun JarInputStream.nextPropertiesLine(): String? {
        var line = aliasReader?.readLine()
        while (line == null) {
            if (!updatePropertiesReader(this)) {
                return null
            }
            line = aliasReader!!.readLine()
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
                        "DSLReference<${resultClassName(parentName)}>"
                    } else if (attr.name.endsWith("pathref")) {
                        "DSLReference<DSLPath>"
                    } else if (attr.name == "loaderref") {
                         "DSLLoaderRef"
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
        val projectShorten = res.importManager.shorten(ANT_CLASS_PREFIX + "Project")
        val targetShorten = res.importManager.shorten(ANT_CLASS_PREFIX + "Target")
        val rcShorten = res.importManager.shorten(ANT_CLASS_PREFIX + "RuntimeConfigurable")
        res.append("public class $dslTypeName(projectAO: $projectShorten, targetAO: $targetShorten, parentWrapperAO: $rcShorten?, elementTag: String, nearestExecutable: DSLTask?)")
        res.append(": ${if (isTaskContainer) {"DSLTaskContainerTask"} else {"DSLTask"}}(projectAO, targetAO, parentWrapperAO, elementTag, nearestExecutable)")
        if (isTextContainer) {
            res.append(",\n        DSLTextContainer")
        }
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
            return "DSLReference<$typeName>"
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
        val dslTypeName = resultClassName(className)
        val initReceiverTypeName = if (isTaskContainer) {
            "DSLTaskContainer"
        } else {
            dslTypeName
        }
        out.append("\n")
        out.append("public fun $parentName.`$tag`(\n")
        renderConstructorParameters(out)
        if (withInit) {
            if (!attributes.empty) {
                out.append(",\n")
            }
            out.append("        init: $initReceiverTypeName.() -> ${out.importManager.shorten("kotlin.Unit")}")
        }
        out.append(")")
        val returnType = constructorReturnType()
        if (returnType != null) {
            out.append(": " + out.importManager.shorten(returnType))
        }
        out.append(" {\n")
        if (withInit) {
            val mustBeExecuted = (parentName == "DSLTaskContainer" || parentName == "DSLProject")
            out.append("    val dslObject = $dslTypeName(this.projectAO, this.targetAO, ${if (mustBeExecuted) {"null"} else {"this.wrapperAO"}}, \"$tag\", ")
            out.append(if (mustBeExecuted) {"null"} else {"this"})
            out.append(")\n")
            for (attr in attributes) {
                val dslAttr = dslAttribute(attr, className)
                out.append("    if (`${dslAttr.name}` != null) { dslObject.`${dslAttr.name}` = `${dslAttr.name}` }\n")
            }

            if (!isTaskContainer) {
                out.append("    dslObject.init()\n")
            }
            if (returnType != null) {
                out.append("    val reference = DSLReference(dslObject)\n")
            }
            out.append("    dslObject.configure()\n")
            if (isTaskContainer) {
                out.append("    dslObject.addTaskContainer(dslObject, init)\n")
            }
            if (mustBeExecuted) {
                out.append("    dslObject.execute()\n")
            }
            if (returnType != null) {
                out.append("    return reference\n")
            }
        } else {
            out.append("    return `$tag`(\n")
            for (attr in attributes) {
                out.append("        `${attr.name}`,\n")
            }
            out.append("        {$initReceiverTypeName.() -> })\n")
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
            if (invAntClass.isTask) {
                invAntClass.renderNestedElement(invKotlinSrcFile, "DSLTaskContainer", Attribute(tag, className))
            } else  if (invAntClass.hasRefId) {
                invAntClass.renderNestedElement(invKotlinSrcFile, "DSLProject", Attribute(tag, className))
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
        return "DSL" + shortName + "Container"
    }

    public fun typeNeedsContainer(name: String): Boolean {
        return !name.startsWith("java.")
    }

    public fun resolveContainer(name: String) {
        if (!resolved.contains(name) && typeNeedsContainer(name)) {
            kotlinSrcFile.append("\ntrait ${containerName(name)} : DSLTask\n")
            resolved.add(name)
        }
    }

    public fun dump(file: File) {
        kotlinSrcFile.dump(file)
    }
}
