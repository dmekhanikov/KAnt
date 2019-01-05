package jetbrains.kant

import jetbrains.kant.common.createClassLoader
import jetbrains.kant.dsl.DSLProject
import org.kohsuke.args4j.Argument
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import java.lang.reflect.InvocationTargetException

fun main(args: Array<String>) {
    Kant().doMain(args)
}

class Kant {
    @Option(name = "-cp", metaVar = "<path>", usage = "Classpath")
    private var classpath = ""

    @Argument(index = 0, metaVar = "project name", usage = "Fully qualified name of the project that must be performed",
            required = true)
    private var className: String? = null

    private fun getterName(fieldName: String): String {
        return "get" + Character.toUpperCase(fieldName[0]) + fieldName.substring(1)
    }

    private fun failWithError(message: String) {
        System.err.println(message)
        System.exit(1)
    }

    fun doMain(args: Array<String>) {
        val parser = CmdLineParser(this)
        try {
            parser.parseArgument(args.toList())
        } catch (e: CmdLineException) {
            System.err.println(e.message)
            System.err.println("Usage: java jetbrains.kant.KantPackage <options> <project name>")
            parser.printUsage(System.err)
            System.err.println()
            return
        }
        val classLoader = createClassLoader(classpath, javaClass.classLoader)
        try {
            val classObject = classLoader.loadClass(className!!)!!
            val project = classObject.getField("INSTANCE$")!!.get(null) as DSLProject
            project.perform()
        } catch (e: ClassNotFoundException) {
            System.err.println("Class " + className + " was not found")
        } catch (e: IllegalAccessException) {
            System.err.println("Cannot get the specified object")
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            System.err.println("Cannot get the specified object")
            e.cause?.printStackTrace()
        }
    }
}
