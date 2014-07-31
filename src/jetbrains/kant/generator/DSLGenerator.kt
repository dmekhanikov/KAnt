package jetbrains.kant.generator

import jetbrains.kant.*
import java.util.HashMap
import java.util.HashSet
import java.util.regex.Pattern
import java.util.jar.JarInputStream
import java.io.*
import java.io.File.pathSeparator
import org.kohsuke.args4j.*
import java.util.ArrayList

public val DSL_ROOT: String = "dsl/src/"
val DSL_PACKAGE = "jetbrains.kant.dsl"
val BASE_DSL_FILE_NAMES = array("Base.kt", "LazyTask.kt", "Misc.kt", "Properties.kt")

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

class DSLGenerator(resultRoot: String, val classpath: Array<String>, aliasFiles: Array<String>, val seekForAliasFiles: Boolean = false) {
    private val resolved = HashMap<String, Target>()
    private val constructors = HashMap<String, HashSet<String>>()
    private val containerGenerator = ContainerGenerator()
    private var classLoader: ClassLoader = createClassLoader(classpath)
    private val resultRoot = resultRoot + if (resultRoot.endsWith('/')) {""} else {'/'}
    private val aliasFiles = ArrayList<String>(); {
        this.aliasFiles.addAll(aliasFiles)
    }

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
                }  catch(e: NoClassDefFoundError) {
                    antClass = null
                }
                if (antClass != null) {
                    val target = Target(className)
                    target.antClass = antClass
                    resolved[className] = target
                    val resultClassName = resultClassName(className)
                    val pkg = resultClassName.substring(0, resultClassName.lastIndexOf("."))
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
        if (seekForAliasFiles) {
            for (jar in classpath) {
                if (!jar.endsWith(".jar")) {
                    continue
                }
                val jis = JarInputStream(FileInputStream(jar))
                aliasFiles.addAll(jis.getAliasFiles())
                jis.close()
            }
        }
        val aliases = ArrayList<Alias>()
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
            if (aliasFileName.toLowerCase().endsWith(".properties")) {
                aliases.addAll(AliasParser(stream!!).parseProperties())
            } else if (aliasFileName.toLowerCase().endsWith(".xml")) {
                aliases.addAll(AliasParser(stream!!).parseXML())
            }
        }
        for (alias in aliases) {
            resolveAlias(alias)
        }
        for (target in resolved.values()) {
            target.dumpFile()
        }
        containerGenerator.dump(File(generatedRoot + "/Containers.kt"))
    }

    private fun resolveAlias(alias: Alias) {
        val tag = alias.tag
        val className = alias.className
        val topLevel = alias.topLevel
        resolveClass(className)
        resolved[className]?.generateConstructors(tag, topLevel)
    }

    private fun resultClassName(srcClassName: String): String {
        val className = srcClassName.replace("$", "")
        val result = StringBuilder()
        result.append(DSL_PACKAGE + ".")
        if (srcClassName.startsWith(ANT_CLASS_PREFIX)) {
            result.append(className.substring(ANT_CLASS_PREFIX.length))
        } else {
            result.append("other." + className)
        }
        result.insert(result.lastIndexOf(".") + 1, "DSL")
        return result.toString()
    }

    private fun resultFile(srcClassName: String): File {
        return File(resultRoot + resultClassName(srcClassName)
                .replace(DSL_PACKAGE, DSL_PACKAGE + ".generated").replace('.', '/') + ".kt")
    }

    private fun JarInputStream.getAliasFiles(): List<String> {
        val result = ArrayList<String>()
        var jarEntry = getNextJarEntry()
        while (jarEntry != null) {
            val entryName = jarEntry!!.getName()
            if (entryName.toLowerCase().endsWith(".properties") || entryName.toLowerCase().endsWith(".xml")) {
                result.add(entryName)
            }
            jarEntry = getNextJarEntry()
        }
        return result
    }

    private fun dslAttribute(attr: Attribute, parentName: String): Attribute {
        val name = attr.name
        val typeName =
                if (PRIMITIVE_TYPES.contains(attr.typeName)) {
                    attr.typeName
                } else if (attr.typeName == ANT_CLASS_PREFIX + "types.Reference") {
                    if (attr.name == "refid") {
                        "$DSL_PACKAGE.DSLReference<${resultClassName(parentName)}>"
                    } else if (attr.name.endsWith("pathref")) {
                        "$DSL_PACKAGE.DSLReference<$DSL_PACKAGE.types.DSLPath>"
                    } else if (attr.name == "loaderref") {
                        "$DSL_PACKAGE.DSLLoaderRef"
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
        val dslTaskShorten = res.importManager.shorten("$DSL_PACKAGE.DSLTask")
        val dslTextContainerShorten = res.importManager.shorten("$DSL_PACKAGE.DSLTextContainer")
        val dslTaskContainerTaskShorten = res.importManager.shorten("$DSL_PACKAGE.DSLTaskContainerTask")
        val dslConditionShorten = res.importManager.shorten("$DSL_PACKAGE.DSLCondition")
        res.append("public class $dslTypeName(projectAO: $projectShorten, targetAO: $targetShorten, parentWrapperAO: $rcShorten?, elementTag: String, nearestExecutable: $dslTaskShorten?)")
        res.append(": ${if (isTaskContainer) {dslTaskContainerTaskShorten} else {dslTaskShorten}}(projectAO, targetAO, parentWrapperAO, elementTag, nearestExecutable)")
        if (isTextContainer) {
            res.append(",\n        $dslTextContainerShorten")
        }
        if (isCondition) {
            res.append(",\n        $dslConditionShorten")
        }
        for (nestedType in nestedTypes) {
            val containerName = res.importManager.shorten(containerGenerator.containerName(nestedType))
            res.append(",\n        $containerName")
        }
        res.append(" {\n")
        for (attr in attributes) {
            val dslAttr = dslAttribute(attr, className)
            res.append("    var ${escapeKeywords(dslAttr.name)}: ${res.importManager.shorten(dslAttr.typeName.replace('$', '.'))} " +
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
        if (isCondition) {
            return "Boolean"
        } else if (hasRefId) {
            return "$DSL_PACKAGE.DSLReference<$typeName>"
        } else {
            return null
        }
    }

    private fun AntClass.renderNestedElement(out: KotlinSourceFile,
                                             parentName: String, element: Attribute) {
        if (constructorIsGenerated(parentName, element.name)) {
            return
        }
        resolveClass(element.typeName)
        val elementClass = resolved[explodeTypeName(element.typeName)[0]]!!.antClass!!
        elementClass.renderConstructor(out, parentName, element.name, true)
        elementClass.renderConstructor(out, parentName, element.name, false)
        addConstructor(parentName, element.name)
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
            out.append("        ${escapeKeywords(dslAttr.name)}: ${out.importManager.shorten(dslAttr.typeName.replace('$', '.'))}? = null")
        }
    }

    private fun AntClass.renderConstructor(out: KotlinSourceFile,
                                           parentName: String, tag: String, withInit: Boolean) {
        val dslTypeName = out.importManager.shorten(resultClassName(className))
        val dslTaskContainerShorten = out.importManager.shorten("$DSL_PACKAGE.DSLTaskContainer")
        val dslReferenceShorten = out.importManager.shorten("$DSL_PACKAGE.DSLReference")
        val initReceiverTypeName = if (isTaskContainer) {
            dslTaskContainerShorten
        } else {
            dslTypeName
        }
        out.append("\n")
        out.append("public fun ${out.importManager.shorten(parentName)}.${escapeKeywords(tag)}(\n")
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
            val mustBeExecuted = (parentName == "$DSL_PACKAGE.DSLTaskContainer" || parentName == "$DSL_PACKAGE.DSLProject")
            out.append("    val dslObject = $dslTypeName(this.projectAO, this.targetAO, ${if (mustBeExecuted) {"null"} else {"this.wrapperAO"}}, \"$tag\", ")
            out.append(if (mustBeExecuted) {"null"} else {"this"})
            out.append(")\n")
            for (attr in attributes) {
                val dslAttr = dslAttribute(attr, className)
                out.append("    if (${escapeKeywords(dslAttr.name)} != null) { dslObject.${escapeKeywords(dslAttr.name)} = ${escapeKeywords(dslAttr.name)} }\n")
            }

            if (!isTaskContainer) {
                out.append("    dslObject.init()\n")
            }
            if (!isCondition && returnType != null) {
                out.append("    val reference = $dslReferenceShorten(dslObject)\n")
            }
            out.append("    dslObject.configure()\n")
            if (isTaskContainer) {
                out.append("    dslObject.addTaskContainer(dslObject, init)\n")
            }
            if (mustBeExecuted && !isCondition) {
                out.append("    dslObject.execute()\n")
            }
            if (returnType != null) {
                out.append("    return ")
                out.append(if (isCondition) {
                    "dslObject.eval()"
                } else {
                    "reference"
                }).append("\n")
            }
        } else {
            out.append("    return ${escapeKeywords(tag)}(\n")
            for (attr in attributes) {
                out.append("        ${escapeKeywords(attr.name)},\n")
            }
            out.append("        {$initReceiverTypeName.() -> })\n")
        }
        out.append("}\n")
    }

    private fun cutShortName(name: String): String {
        return name.substring(name.lastIndexOf('.') + 1).replace("$", "")
    }

    private fun constructorIsGenerated(parent: String, function: String): Boolean {
        val functions = constructors[parent]
        return functions != null && functions.contains(function)
    }

    private fun addConstructor(parent: String, function: String) {
        var functions = constructors[parent]
        if (functions == null) {
            functions = HashSet()
            constructors.put(parent, functions!!)
        }
        functions!!.add(function)
    }

    private inner class Target(val className: String) {
        var antClass: AntClass? = null
        var kotlinSrcFile: KotlinSourceFile? = null

        fun generateConstructors(tag: String, topLevel: Boolean) {
            val invAntClass = antClass!!
            val invKotlinSrcFile = kotlinSrcFile!!
            if (topLevel) {
                if (invAntClass.isTask || invAntClass.isCondition) {
                    invAntClass.renderNestedElement(invKotlinSrcFile, "$DSL_PACKAGE.DSLTaskContainer", Attribute(tag, className))
                } else if (invAntClass.hasRefId) {
                    invAntClass.renderNestedElement(invKotlinSrcFile, "$DSL_PACKAGE.DSLProject", Attribute(tag, className))
                }
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
}

class ContainerGenerator {
    private val kotlinSrcFile = KotlinSourceFile("$DSL_PACKAGE.containers")
    private val resolved = HashSet<String>()

    public fun resolveContainers(names: List<String>) {
        for (name in names) {
            resolveContainer(name)
        }
    }

    public fun containerName(name: String): String {
        val shortName = name.substring(name.lastIndexOf('.') + 1).replace("$", "")
        return "$DSL_PACKAGE.containers.DSL" + shortName + "Container"
    }

    public fun typeNeedsContainer(name: String): Boolean {
        return !name.startsWith("java.")
    }

    public fun resolveContainer(name: String) {
        if (!resolved.contains(name) && typeNeedsContainer(name)) {
            val containerName = kotlinSrcFile.importManager.shorten(containerName(name))
            val dslTaskShorten =  kotlinSrcFile.importManager.shorten("$DSL_PACKAGE.DSLTask")
            kotlinSrcFile.append("\ntrait $containerName : $dslTaskShorten\n")
            resolved.add(name)
        }
    }

    public fun dump(file: File) {
        kotlinSrcFile.dump(file)
    }
}
