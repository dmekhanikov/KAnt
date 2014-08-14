package jetbrains.kant.generator

import jetbrains.kant.gtcommon.constants.*
import jetbrains.kant.gtcommon.*
import jetbrains.kant.common.createClassLoader
import jetbrains.kant.common.valuesToMap
import java.io.*
import java.io.File.pathSeparator
import org.kohsuke.args4j.*
import java.util.HashMap
import java.util.HashSet
import java.util.ArrayList
import java.util.regex.Pattern
import java.util.jar.JarInputStream

fun main(args: Array<String>) {
    GeneratorRunner().doMain(args)
}

class GeneratorRunner {
    [Option(name = "-cp", metaVar = "<path>",usage = "Classpath")]
    private val classpath: String = ""

    [Option(name = "-d", metaVar = "<directory>", usage = "Output directory", required = true)]
    private val outDir: String = ""

    [Option(name = "-seek", usage = "Seek alias files")]
    private val seek = false

    [Option(name = "-defaultaliases", usage = "Generate functions with names similar to class names")]
    private val defAl = false

    [Option(name = "-compile", usage = "Compile the library after generation")]
    private val compile = false

    [Option(name = "-jar", usage = "Create jar file containing all class files of the generated library and some information about its structure")]
    private val createJar = false

    [Argument(metaVar = "alias files", usage = "Antlib or properties files with aliases that the generator should use")]
    private val aliasFiles: Array<String> = array()

    fun doMain(args: Array<String>) {
        val parser = CmdLineParser(this)
        try {
            parser.parseArgument(args.toArrayList())
        } catch(e: CmdLineException) {
            System.err.println(e.getMessage())
            System.err.println("Usage: java jetbrains.kant.generator.GeneratorPackage <options> <alias files>")
            parser.printUsage(System.err)
            System.err.println()
            return
        }
        val generator = DSLGenerator(outDir, classpath, aliasFiles, seek, defAl)
        generator.generate()
        if (compile || createJar) {
            generator.compile()
        }
        if (createJar) {
            generator.createJar()
        }
    }
}

private fun copyBaseFiles(src: String, dst: String) {
    for (file in File(src).listFiles { it != File(DSL_GENERATED_DIR) }!!) {
        val dstFile = dst + file.name
        if (file.isDirectory()) {
            copyBaseFiles(file.toString(), dstFile + "/")
        } else {
            file.copyTo(File(dstFile))
        }
    }
}

class DSLGenerator(outDir: String, val classpath: String, aliasFiles: Array<String>,
                   val seekForAliasFiles: Boolean = false, val defAl: Boolean = false) {
    private val resolved = HashMap<String, Target>()
    public val structure: HashMap<String, DSLClass> = HashMap()
    private val containerGenerator = ContainerGenerator()
    private var classLoader: ClassLoader = createClassLoader(classpath, null)
    private val outDir = outDir + if (outDir.endsWith('/')) {""} else {'/'}
    private val srcOutDir = this.outDir + "src/"
    private val binOutDir = this.outDir + "bin/"
    private val distOutDir = this.outDir + "dist/"
    private val aliasFiles = ArrayList<String>(); {
        this.aliasFiles.addAll(aliasFiles)
        structure.put(DSL_TASK_CONTAINER, DSLClass(DSL_TASK_CONTAINER, ArrayList<String>()))
        structure.put(DSL_PROJECT, DSLClass(DSL_PROJECT, array(DSL_TASK_CONTAINER).toList()))
        structure.put(DSL_TARGET, DSLClass(DSL_TARGET, array(DSL_TASK_CONTAINER).toList()))
    }

    private var aliasReader: BufferedReader? = null

    private val PRIMITIVE_TYPES = array("Boolean", "Char", "Byte", "Short", "Int", "Float", "Long", "Double").toSet()

    private fun createAntClass(className: String): AntClass? {
        try {
            return AntClass(classLoader, className)
        } catch (e: ClassNotFoundException) {
            return null
        } catch(e: NoClassDefFoundError) {
            return null
        } catch(e: IllegalAccessError) {
            return null
        }
    }

    private fun addTarget(antClass: AntClass) {
        val className = antClass.className
        val target = Target(className)
        target.antClass = antClass
        resolved[className] = target
        val dslClassName = resultClassName(antClass.className)
        structure.put(dslClassName,
                DSLClass(dslClassName, antClass.nestedTypes.map { containerGenerator.containerName(it) }))
        val resultClassName = resultClassName(className)
        val pkg = resultClassName.substring(0, resultClassName.lastIndexOf("."))
        target.kotlinSrcFile = antClass.toKotlin(pkg)
        containerGenerator.resolveContainers(antClass.implementedInterfaces)
        containerGenerator.resolveContainers(antClass.nestedTypes)
    }

    public fun resolveClass(className: String) {
        val names = explodeTypeName(className)
        for (name in names) {
            if (!resolved.containsKey(name)) {
                val antClass = createAntClass(className)
                if (antClass != null) {
                    addTarget(antClass)
                }
            }
        }
    }

    public fun generate() {
        copyBaseFiles(DSL_SRC_ROOT, srcOutDir)
        val generatedRoot = srcOutDir + DSL_PACKAGE.replace('.', '/') + "/generated"
        File(generatedRoot).mkdirs()
        val classpathArray = classpath.split(pathSeparator)
        if (seekForAliasFiles) {
            for (jar in classpathArray) {
                if (jar.endsWith(".jar")) {
                    val jis = JarInputStream(FileInputStream(jar))
                    aliasFiles.addAll(jis.getAliasFiles())
                    jis.close()
                }
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
        if (defAl) {
            for (jar in classpathArray) {
                if (jar.endsWith(".jar")) {
                    val jis = JarInputStream(FileInputStream(jar))
                    val antTasks = jis.getAntTasks()
                    for (antTask in antTasks) {
                        val className = antTask.className
                        val taskName = defaultConstructorName(className)
                        val dslClassName = resultClassName(className)
                        if (!resolved.contains(className)) {
                            addTarget(antTask)
                        }
                        if (!constructorIsGenerated(dslClassName, taskName)) {
                            resolved[antTask.className]?.generateConstructors(taskName, false)
                        }
                    }
                    jis.close()
                }
            }
        }
        for (target in resolved.values()) {
            target.dumpFile()
        }
        containerGenerator.dump(File(generatedRoot + "/Containers.kt"))
    }

    public fun compile() {
        val depends = array(classpath, ARGS4J_JAR).join(pathSeparator)
        compileKotlinCode(depends, binOutDir, srcOutDir, COMMON_SRC_DIR)
        val outFile = File(binOutDir + STRUCTURE_FILE)
        outFile.getParentFile()!!.mkdirs()
        val fileOut = FileOutputStream(outFile)
        val objectOutputStream = ObjectOutputStream(fileOut)
        objectOutputStream.writeObject(structure)
        objectOutputStream.close()
        fileOut.close()
    }

    public fun createJar() {
        val jarFile = distOutDir + "kant.jar"
        createJar(jarFile, binOutDir)
    }

    private fun resolveAlias(alias: Alias) {
        val tag = alias.tag
        val className = alias.className
        val restricted = alias.restricted
        resolveClass(className)
        resolved[className]?.generateConstructors(tag, restricted)
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
        return File(srcOutDir + resultClassName(srcClassName)
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

    private fun JarInputStream.getAntTasks(): List<AntClass> {
        val result = ArrayList<AntClass>()
        var jarEntry = getNextJarEntry()
        while (jarEntry != null) {
            val entryName = jarEntry!!.getName()
            if (entryName.endsWith(".class") && !entryName.contains("$")) {
                val className = entryName.substring(0, entryName.lastIndexOf('.')).replace('/', '.')
                val antClass = createAntClass(className)
                if (antClass != null && (antClass.isTask || antClass.isCondition || antClass.hasRefId)) {
                    result.add(antClass)
                }
            }
            jarEntry = getNextJarEntry()
        }
        return result
    }

    private fun dslAttribute(attr: AntAttribute, parentName: String): AntAttribute {
        val name = attr.name
        val typeName =
                if (PRIMITIVE_TYPES.contains(attr.typeName)) {
                    attr.typeName
                } else if (attr.typeName == ANT_CLASS_PREFIX + "types.Reference") {
                    if (attr.name == "refid") {
                        "$DSL_REFERENCE<${resultClassName(parentName)}>"
                    } else if (attr.name.toLowerCase().endsWith("pathref")) {
                        "$DSL_REFERENCE<$DSL_PATH>"
                    } else {
                        "String"
                    }
                } else {
                    "String"
                }
        return AntAttribute(name, typeName)
    }

    private fun AntClass.toKotlin(pkg: String?): KotlinSourceFile {
        val res = KotlinSourceFile(pkg)
        val dslTypeName = "$pkg.DSL" + cutShortName(className)
        val dslTypeNameShorten = res.importManager.shorten(dslTypeName)
        val projectShorten = res.importManager.shorten(ANT_CLASS_PREFIX + "Project")
        val targetShorten = res.importManager.shorten(ANT_CLASS_PREFIX + "Target")
        val rcShorten = res.importManager.shorten(ANT_CLASS_PREFIX + "RuntimeConfigurable")
        val dslTaskShorten = res.importManager.shorten(DSL_TASK)
        val dslTextContainerShorten = res.importManager.shorten(DSL_TEXT_CONTAINER)
        val dslTaskContainerTaskShorten = res.importManager.shorten(DSL_TASK_CONTAINER_TASK)
        val dslConditionShorten = res.importManager.shorten(DSL_CONDITION)
        res.append("public class $dslTypeNameShorten(projectAO: $projectShorten, targetAO: $targetShorten, parentWrapperAO: $rcShorten?, elementTag: String, nearestExecutable: $dslTaskShorten?)")
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
            renderNestedElement(res, dslTypeName, element, false, false)
        }
        return res
    }

    private fun AntClass.constructorReturnType(): String? {
        val typeName = resultClassName(className)
        if (isCondition) {
            return "Boolean"
        } else if (hasRefId) {
            return "$DSL_REFERENCE<$typeName>"
        } else {
            return null
        }
    }

    private fun AntClass.renderNestedElement(out: KotlinSourceFile,
                                             parentName: String, element: AntAttribute,
                                             define: Boolean, restricted: Boolean) {
        if (constructorIsGenerated(parentName, element.name)) {
            return
        }
        resolveClass(element.typeName)
        val elementClass = resolved[explodeTypeName(element.typeName)[0]]!!.antClass!!
        elementClass.renderConstructor(out, parentName, element.name, define, restricted, true)
        elementClass.renderConstructor(out, parentName, element.name, define, restricted, false)
        addConstructor(parentName, element.name, out.pkg, elementClass)
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
                                           parentName: String, tag: String,
                                           define: Boolean, restricted: Boolean, withInit: Boolean) {
        val dslTypeName = out.importManager.shorten(resultClassName(className))
        val dslTaskContainerShorten = out.importManager.shorten(DSL_TASK_CONTAINER)
        val dslReferenceShorten = out.importManager.shorten(DSL_REFERENCE)
        val initReceiverTypeName = if (isTaskContainer) {
            dslTaskContainerShorten
        } else {
            dslTypeName
        }
        out.append("\n")
        out.append("public fun ${out.importManager.shorten(parentName)}.${escapeKeywords(toCamelCase(tag))}(\n")
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
            val mustBeExecuted = (parentName == DSL_TASK_CONTAINER || parentName == DSL_PROJECT)
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
            if (define) {
                if (restricted) {
                    out.append("    dslObject.defineType(\"$className\")\n")
                } else {
                    out.append("    dslObject.defineComponent(\"$className\")\n")
                }
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
            out.append("    return ${toCamelCase(tag)}(\n")
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
        val dslClass = structure[parent]!!
        return dslClass.containsFunction(function)
    }

    private fun defaultConstructorName(className: String): String {
        val shortName = cutShortName(className)
        return Character.toLowerCase(shortName.charAt(0)) + shortName.substring(1)
    }

    private fun addConstructor(parent: String, function: String, pkg: String?, antClass: AntClass) {
        val dslClass = structure[parent]!!
        dslClass.addFunction(function, pkg,
                antClass.attributes.map { dslAttribute(it, antClass.className) },
                resultClassName(antClass.className))
    }

    private inner class Target(val className: String) {
        var antClass: AntClass? = null
        var kotlinSrcFile: KotlinSourceFile? = null

        fun generateConstructors(tag: String, restricted: Boolean) {
            val invAntClass = antClass!!
            val invKotlinSrcFile = kotlinSrcFile!!
            if (!restricted) {
                if (invAntClass.isTask || invAntClass.isCondition || invAntClass.hasRefId) {
                    invAntClass.renderNestedElement(invKotlinSrcFile, DSL_TASK_CONTAINER, AntAttribute(tag, className), true, restricted)
                }
            }
            for (interface in invAntClass.implementedInterfaces) {
                if (containerGenerator.typeNeedsContainer(interface)) {
                    val containerName = containerGenerator.containerName(interface)
                    invAntClass.renderNestedElement(invKotlinSrcFile, containerName, AntAttribute(tag, className), true, restricted)
                }
            }
        }

        fun dumpFile() {
            kotlinSrcFile!!.dump(resultFile(className))
        }
    }

    private inner class ContainerGenerator {
        private val kotlinSrcFile = KotlinSourceFile("$DSL_PACKAGE.containers")
        private val resolved = HashSet<String>()

        public fun resolveContainers(names: List<String>) {
            for (name in names) {
                resolveContainer(name)
            }
        }

        public fun containerName(name: String): String {
            val shortName = name.substring(name.lastIndexOf('.') + 1).replace("$", "")
            return "$DSL_PACKAGE.containers._DSL" + shortName + "Container"
        }

        public fun typeNeedsContainer(name: String): Boolean {
            return !name.startsWith("java.")
        }

        public fun resolveContainer(name: String) {
            if (!resolved.contains(name) && typeNeedsContainer(name)) {
                val containerName = containerName(name)
                val containerNameShorten = kotlinSrcFile.importManager.shorten(containerName)
                val dslTaskShorten =  kotlinSrcFile.importManager.shorten(DSL_TASK)
                kotlinSrcFile.append("\ntrait $containerNameShorten : $dslTaskShorten\n")
                resolved.add(name)
                structure.put(containerName, DSLClass(containerName, ArrayList<String>()))
            }
        }

        public fun dump(file: File) {
            kotlinSrcFile.dump(file)
        }
    }
}

class DSLClass(val name: String, val traits: List<String>): Serializable {
    private val functions = HashMap<String, DSLFunction>()

    fun addFunction(functionName: String, pkg: String?, attributes: List<AntAttribute>, receiver: String) {
        functions.put(functionName.toLowerCase(), DSLFunction(toCamelCase(functionName), pkg, attributes.valuesToMap {it.name.toLowerCase()}, name, receiver))
    }

    fun containsFunction(name: String): Boolean {
        return functions.containsKey(name.toLowerCase())
    }

    fun getFunction(name: String): DSLFunction? {
        return functions[name.toLowerCase()]
    }
}

class DSLFunction(val name: String, val pkg: String?, val attributes: Map<String, AntAttribute>, val parentName: String, val initReceiver: String): Serializable {
    public fun getAttribute(attributeName: String): AntAttribute? {
        return attributes[attributeName.toLowerCase()]
    }

    public fun getAttributeName(attributeName: String): String? {
        return getAttribute(attributeName)?.name;
    }

    public fun getAttributeType(attributeName: String): String? {
        return getAttribute(attributeName)?.typeName
    }
}
