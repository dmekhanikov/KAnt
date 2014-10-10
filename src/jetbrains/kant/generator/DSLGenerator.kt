package jetbrains.kant.generator

import jetbrains.kant.gtcommon.constants.*
import jetbrains.kant.gtcommon.*
import jetbrains.kant.common.createClassLoader
import jetbrains.kant.common.valuesToMap
import jetbrains.kant.common.DefinitionKind
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

    [Option(name = "-seek", usage = "Seek definition files")]
    private val seek = false

    [Option(name = "-defaultnames", usage = "Generate functions with names similar to class names")]
    private val defaultNames = false

    [Option(name = "-compile", usage = "Compile the library after generation")]
    private val compile = false

    [Option(name = "-jar", usage = "Create jar file containing all class files of the generated library and some information about its structure")]
    private val createJar = false

    [Argument(metaVar = "definition files", usage = "Antlib or properties files with definitions that the generator should use")]
    private val definitionFiles: Array<String> = array()

    fun doMain(args: Array<String>) {
        val parser = CmdLineParser(this)
        try {
            parser.parseArgument(args.toArrayList())
        } catch(e: CmdLineException) {
            System.err.println(e.getMessage())
            System.err.println("Usage: java jetbrains.kant.generator.GeneratorPackage <options> <definition files>")
            parser.printUsage(System.err)
            System.err.println()
            return
        }
        val generator = DSLGenerator(outDir, classpath, definitionFiles, seek, defaultNames)
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

private fun JarInputStream.getAntlibFiles(): List<String> {
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

private fun JarInputStream.getAntTasks(classLoader: ClassLoader): List<AntClass> {
    val result = ArrayList<AntClass>()
    var jarEntry = getNextJarEntry()
    while (jarEntry != null) {
        val entryName = jarEntry!!.getName()
        if (entryName.endsWith(".class") && !entryName.contains("$")) {
            val className = entryName.substring(0, entryName.lastIndexOf('.')).replace('/', '.')
            val antClass = tryToCreateAntClass(className, classLoader)
            if (antClass != null && (antClass.isTask || antClass.isCondition || antClass.hasRefId)) {
                result.add(antClass)
            }
        }
        jarEntry = getNextJarEntry()
    }
    return result
}

private fun tryToCreateAntClass(className: String, classLoader: ClassLoader): AntClass? {
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

class DSLGenerator(outDir: String, val classpath: String, definitionFiles: Array<String>,
                   val seekForAntlibFiles: Boolean = false, val defaultNames: Boolean = false) {
    private val resolved = HashMap<String, Target>()
    public val structure: HashMap<String, DSLClass> = HashMap()
    private val containerGenerator = ContainerGenerator()
    private var classLoader: ClassLoader = createClassLoader(classpath, null)
    private val outDir = outDir + if (outDir.endsWith('/')) {""} else {'/'}
    private val srcOutDir = this.outDir + "src/"
    private val binOutDir = this.outDir + "bin/"
    private val distOutDir = this.outDir + "dist/"
    private val antlibFiles = ArrayList<String>(); {
        this.antlibFiles.addAll(definitionFiles)
        structure.put(DSL_TASK_CONTAINER, DSLClass(DSL_TASK_CONTAINER, ArrayList<String>()))
        structure.put(DSL_PROJECT, DSLClass(DSL_PROJECT, array(DSL_TASK_CONTAINER).toList()))
        structure.put(DSL_TARGET, DSLClass(DSL_TARGET, array(DSL_TASK_CONTAINER).toList()))
    }

    private val PRIMITIVE_TYPES = array("Boolean", "Char", "Byte", "Short", "Int", "Float", "Long", "Double").toSet()

    public fun generate() {
        copyBaseFiles(DSL_SRC_ROOT, srcOutDir)
        val generatedRoot = srcOutDir + DSL_PACKAGE.replace('.', '/') + "/generated"
        File(generatedRoot).mkdirs()

        val definitions = getDefinitions()
        for (definition in definitions) {
            definition.resolve()
        }

        if (defaultNames) {
            generateConstructorsWithDefaultNames()
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

    private fun resolveClass(className: String) {
        if (!resolved.containsKey(className)) {
            val antClass = tryToCreateAntClass(className, classLoader)
            if (antClass != null) {
                resolveClass(antClass)
            }
        }
    }

    private fun resolveClass(antClass: AntClass) {
        val target = Target(antClass)
        val className = antClass.className
        resolved[className] = target
        val dslClassName = getResultClassName(className)
        structure.put(dslClassName,
                DSLClass(dslClassName, antClass.nestedTypes.map { containerGenerator.getContainerName(it) }))
        val resultClassName = getResultClassName(className)
        val pkg = resultClassName.substring(0, resultClassName.lastIndexOf("."))
        target.kotlinSrcFile = antClass.toKotlin(pkg)
        containerGenerator.generateContainers(antClass.implementedInterfaces)
        containerGenerator.generateContainers(antClass.nestedTypes)
    }

    private fun generateConstructorsWithDefaultNames() {
        val classpathArray = classpath.split(pathSeparator)
        for (jar in classpathArray) {
            if (jar.endsWith(".jar")) {
                val jis = JarInputStream(FileInputStream(jar))
                val antTasks = jis.getAntTasks(classLoader)
                for (antTask in antTasks) {
                    val className = antTask.className
                    val taskName = getConstructorDefaultName(className)
                    val dslClassName = getResultClassName(className)
                    if (!resolved.contains(className)) {
                        resolveClass(antTask)
                    }
                    if (!constructorIsGenerated(DSL_TASK_CONTAINER, taskName)) {
                        val definition = Definition(name = taskName, className = className, kind = DefinitionKind.TASK)
                        resolved[antTask.className]?.generateConstructors(definition)
                    }
                }
                jis.close()
            }
        }
    }

    private fun getDefinitions(): List<Definition> {
        val classpathArray = classpath.split(pathSeparator)
        if (seekForAntlibFiles) {
            for (jar in classpathArray) {
                if (jar.endsWith(".jar")) {
                    val jis = JarInputStream(FileInputStream(jar))
                    antlibFiles.addAll(jis.getAntlibFiles())
                    jis.close()
                }
            }
        }
        val definitions = ArrayList<Definition>()
        for (antlibFileName in antlibFiles) {
            var stream = classLoader.getResourceAsStream(antlibFileName)
            if (stream == null) {
                val antlibFile = File(antlibFileName)
                if (!antlibFile.exists()) {
                    System.err.println("File $antlibFile was not found")
                    continue
                }
                stream = FileInputStream(antlibFile)
            }
            if (antlibFileName.toLowerCase().endsWith(".properties")) {
                definitions.addAll(DefinitionParser(stream!!).parseProperties())
            } else if (antlibFileName.toLowerCase().endsWith(".xml")) {
                definitions.addAll(DefinitionParser(stream!!).parseXML())
            }
        }
        return definitions
    }

    private fun Definition.resolve() {
        resolveClass(className)
        resolved[className]?.generateConstructors(this)
    }

    private fun getResultClassName(srcClassName: String): String {
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

    private fun getResultFile(srcClassName: String): File {
        return File(srcOutDir + getResultClassName(srcClassName)
                .replace(DSL_PACKAGE, DSL_PACKAGE + ".generated").replace('.', '/') + ".kt")
    }

    private fun constructDslAttribute(attr: AntAttribute, parentName: String): AntAttribute {
        val name = attr.name
        val typeName =
                if (PRIMITIVE_TYPES.contains(attr.typeName)) {
                    attr.typeName
                } else if (attr.typeName == ANT_CLASS_PREFIX + "types.Reference") {
                    if (attr.name == "refid") {
                        "$DSL_REFERENCE<${getResultClassName(parentName)}>"
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
            val containerName = res.importManager.shorten(containerGenerator.getContainerName(nestedType))
            res.append(",\n        $containerName")
        }
        res.append(" {\n")
        for (attr in attributes) {
            val dslAttr = constructDslAttribute(attr, className)
            res.append("    var ${escapeKeywords(dslAttr.name)}: ${res.importManager.shorten(dslAttr.typeName.replace('$', '.'))} " +
                    "by ${res.importManager.shorten("kotlin.properties.Delegates")}.mapVar(attributes)\n")
        }
        res.append("}\n")
        for (element in nestedElements) {
            val definition = Definition(name = element.name, className = element.typeName, kind = DefinitionKind.NESTED)
            renderNestedElement(res, dslTypeName, definition)
        }
        return res
    }

    private fun AntClass.getConstructorReturnType(): String? {
        val typeName = getResultClassName(className)
        if (isCondition) {
            return "Boolean"
        } else if (hasRefId) {
            return "$DSL_REFERENCE<$typeName>"
        } else {
            return null
        }
    }

    private fun AntClass.renderNestedElement(out: KotlinSourceFile,
                                             parentName: String,
                                             definition: Definition) {
        if (constructorIsGenerated(parentName, definition.name)) {
            return
        }
        resolveClass(definition.className)
        val elementClass = resolved[definition.className]!!.antClass
        elementClass.renderConstructor(out, parentName, definition, true)
        elementClass.renderConstructor(out, parentName, definition, false)
        addConstructor(parentName, definition.name, out.pkg, elementClass)
    }

    private fun AntClass.renderConstructorParameters(out: KotlinSourceFile) {
        var first = true
        for (attr in attributes) {
            val dslAttr = constructDslAttribute(attr, className)
            if (first) {
                first = false
            } else {
                out.append(",\n")
            }
            out.append("        ${escapeKeywords(dslAttr.name)}: ${out.importManager.shorten(dslAttr.typeName.replace('$', '.'))}? = null")
        }
    }

    private fun AntClass.renderConstructor(out: KotlinSourceFile,
                                           parentName: String,
                                           definition: Definition, withInit: Boolean) {
        val dslTypeName = out.importManager.shorten(getResultClassName(className))
        val dslTaskContainerShorten = out.importManager.shorten(DSL_TASK_CONTAINER)
        val dslReferenceShorten = out.importManager.shorten(DSL_REFERENCE)
        val initReceiverTypeName = if (isTaskContainer) {
            dslTaskContainerShorten
        } else {
            dslTypeName
        }
        out.append("\n")
        out.append("public fun ${out.importManager.shorten(parentName)}.${escapeKeywords(toCamelCase(definition.name))}(\n")
        renderConstructorParameters(out)
        if (withInit) {
            if (!attributes.empty) {
                out.append(",\n")
            }
            out.append("        init: $initReceiverTypeName.() -> ${out.importManager.shorten("kotlin.Unit")}")
        }
        out.append(")")
        val returnType = getConstructorReturnType()
        if (returnType != null) {
            out.append(": " + out.importManager.shorten(returnType))
        }
        out.append(" {\n")
        if (withInit) {
            val mustBeExecuted = (parentName == DSL_TASK_CONTAINER || parentName == DSL_PROJECT)
            out.append("    val dslObject = $dslTypeName(this.projectAO, this.targetAO, ${if (mustBeExecuted) {"null"} else {"this.wrapperAO"}}, \"${definition.name}\", ")
            out.append(if (mustBeExecuted) {"null"} else {"this.nearestExecutable"})
            out.append(")\n")
            for (attr in attributes) {
                val dslAttr = constructDslAttribute(attr, className)
                out.append("    if (${escapeKeywords(dslAttr.name)} != null) { dslObject.${escapeKeywords(dslAttr.name)} = ${escapeKeywords(dslAttr.name)} }\n")
            }
            if (!isTaskContainer) {
                out.append("    dslObject.init()\n")
            }
            if (!isCondition && returnType != null) {
                out.append("    val reference = $dslReferenceShorten(dslObject)\n")
            }
            if (definition.kind != DefinitionKind.NESTED) {
                out.append("    dslObject.defineComponent(${definition.getAttributes(out.importManager)})\n")
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
            out.append("    return ${toCamelCase(definition.name)}(\n")
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

    private fun getConstructorDefaultName(className: String): String {
        val shortName = cutShortName(className)
        return Character.toLowerCase(shortName[0]) + shortName.substring(1)
    }

    private fun constructorIsGenerated(parent: String, function: String): Boolean {
        val dslClass = structure[parent]!!
        return dslClass.containsFunction(function)
    }

    private fun addConstructor(parent: String, function: String, pkg: String?, antClass: AntClass) {
        val dslClass = structure[parent]!!
        dslClass.addFunction(
                function,
                pkg,
                antClass.attributes.map { constructDslAttribute(it, antClass.className) },
                getResultClassName(antClass.className),
                antClass.getConstructorReturnType())
    }

    private inner class Target(val antClass: AntClass) {
        val className = antClass.className
        var kotlinSrcFile: KotlinSourceFile? = null

        fun generateConstructors(definition: Definition) {
            val invAntClass = antClass
            val invKotlinSrcFile = kotlinSrcFile!!
            if (definition.kind != DefinitionKind.COMPONENT && definition.kind != DefinitionKind.NESTED) {
                if (invAntClass.isTask || invAntClass.isCondition || invAntClass.hasRefId) {
                    invAntClass.renderNestedElement(invKotlinSrcFile, DSL_TASK_CONTAINER, definition)
                }
            }
            for (interface in invAntClass.implementedInterfaces) {
                if (containerGenerator.typeNeedsContainer(interface)) {
                    val containerName = containerGenerator.getContainerName(interface)
                    invAntClass.renderNestedElement(invKotlinSrcFile, containerName, definition)
                }
            }
        }

        fun dumpFile() {
            kotlinSrcFile!!.dump(getResultFile(className))
        }
    }

    private inner class ContainerGenerator {
        private val kotlinSrcFile = KotlinSourceFile("$DSL_PACKAGE.containers")
        private val generated = HashSet<String>()

        fun generateContainers(names: List<String>) {
            for (name in names) {
                generateContainer(name)
            }
        }

        fun getContainerName(name: String): String {
            val shortName = name.substring(name.lastIndexOf('.') + 1).replace("$", "")
            return "$DSL_PACKAGE.containers._DSL" + shortName + "Container"
        }

        fun typeNeedsContainer(name: String): Boolean {
            return !name.startsWith("java.")
        }

        fun generateContainer(name: String) {
            if (!generated.contains(name) && typeNeedsContainer(name)) {
                val containerName = getContainerName(name)
                val containerNameShorten = kotlinSrcFile.importManager.shorten(containerName)
                val dslTaskShorten =  kotlinSrcFile.importManager.shorten(DSL_TASK)
                kotlinSrcFile.append("\ntrait $containerNameShorten : $dslTaskShorten\n")
                generated.add(name)
                structure.put(containerName, DSLClass(containerName, ArrayList<String>()))
            }
        }

        fun dump(file: File) {
            kotlinSrcFile.dump(file)
        }
    }
}

class Definition(val name: String,
                 val className: String,
                 val kind: DefinitionKind,
                 val extraAttributes: Map<String, String>? = null) {
    fun getAttributes(importManager: ImportManager): String {
        val res = StringBuilder("name = \"$name\", classname = \"$className\"")
        if (extraAttributes != null) {
            for ((attrName, attrVal) in extraAttributes) {
                res.append(", $attrName=\"$attrVal\"")
            }
        }
        val DefinitionKindShorten = importManager.shorten("$COMMON_PACKAGE.DefinitionKind")
        res.append(", kind = ").append(
                when (kind) {
                    DefinitionKind.TASK -> "$DefinitionKindShorten.TASK"
                    DefinitionKind.TYPE -> "$DefinitionKindShorten.TYPE"
                    DefinitionKind.COMPONENT -> "$DefinitionKindShorten.COMPONENT"
                    DefinitionKind.NESTED -> "$DefinitionKindShorten.NESTED"
                }
        )
        return res.toString()
    }
}

public class DSLClass(public val name: String, public val traits: List<String>): Serializable {
    private val functions = HashMap<String, DSLFunction>()

    public fun addFunction(functionName: String, pkg: String?,
                           attributes: List<AntAttribute>, receiver: String, returnType: String? = null) {
        functions.put(functionName.toLowerCase(),
                DSLFunction(toCamelCase(functionName), pkg,
                        attributes.valuesToMap {it.name.toLowerCase()}, name, receiver, returnType))
    }

    public fun containsFunction(name: String): Boolean {
        return functions.containsKey(name.toLowerCase())
    }

    public fun getFunction(name: String): DSLFunction? {
        return functions[name.toLowerCase()]
    }
}

public class DSLFunction(public val name: String,
                         public val pkg: String?,
                         public val attributes: Map<String, AntAttribute>,
                         public val parentName: String,
                         public val initReceiver: String,
                         public val returnType: String? = null): Serializable {
    public fun getAttribute(attributeName: String): AntAttribute? {
        return attributes[attributeName.toLowerCase()]
    }

    public fun getAttributeName(attributeName: String): String? {
        return getAttribute(attributeName)?.name
    }

    public fun getAttributeType(attributeName: String): String? {
        return getAttribute(attributeName)?.typeName
    }
}
