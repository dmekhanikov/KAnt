package jetbrains.kant

import jetbrains.kant.dsl.DSLProject
import org.kohsuke.args4j.*

import java.io.File
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.net.MalformedURLException
import java.net.URL
import java.net.URLClassLoader
import java.util.ArrayList
import jetbrains.kant.common.createClassLoader

fun main(args: Array<String>) {
    Kant().doMain(args)
}

public class Kant {
    [Option(name = "-cp", metaVar = "<path>", usage = "Classpath")]
    private var classpath = ""

    [Argument(index = 0, metaVar = "project name", usage = "Fully qualified name of the project that must be performed",
            required = true)]
    private var className: String? = null

    private fun getterName(fieldName: String): String {
        return "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1)
    }

    private fun failWithError(message: String) {
        System.err.println(message)
        System.exit(1)
    }

    fun doMain(args: Array<String>) {
        val parser = CmdLineParser(this)
        try {
            parser.parseArgument(args.toList())
        } catch(e: CmdLineException) {
            System.err.println(e.getMessage())
            System.err.println("Usage: java jetbrains.kant.KantPackage <options> <project name>")
            parser.printUsage(System.err)
            System.err.println()
            return
        }
        val classLoader = createClassLoader(classpath, javaClass.getClassLoader())
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
            e.getCause()?.printStackTrace()
        }
    }
}
