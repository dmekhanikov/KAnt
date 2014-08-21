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

fun main(args: Array<String>) {
    Kant().doMain(args)
}

public class Kant {
    [Option(name = "-cp", metaVar = "<path>", usage = "Classpath")]
    private var classpath = ""

    [Argument(index = 0, metaVar = "project name", usage = "Fully qualified name of the project that must be performed",
            required = true)]
    private var objectFQName: String? = null

    private fun getClassName(): String {
        val p = objectFQName!!.lastIndexOf('.')
        if (p == -1) {
            return "_DefaultPackage"
        } else {
            return objectFQName!!.substring(0, p)
        }
    }

    private fun getFieldName(): String {
        val p = objectFQName!!.lastIndexOf('.')
        if (p == -1) {
            return objectFQName!!
        } else {
            return objectFQName!!.substring(p + 1)
        }
    }

    private fun getterName(fieldName: String): String {
        return "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1)
    }

    private fun failWithError(message: String) {
        System.err.println(message)
        System.exit(1)
    }

    private fun createClassLoader(): ClassLoader  {
        val path = ArrayList<URL>()
        val jars = classpath.split(File.pathSeparator)
        for (jar in jars) {
            try {
                if (jar.endsWith(".jar")) {
                    path.add(URL("jar:file:" + jar + "!/"))
                } else {
                    path.add(URL("file:" + jar))
                }
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            }
        }
        return URLClassLoader(path.toArray(Array(path.size()) { path[0] }))
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
        val className = getClassName()
        val fieldName = getFieldName()
        val classLoader = createClassLoader()
        try {
            val classObject = classLoader.loadClass(className)!!
            val projectGetter = classObject.getMethod(getterName(fieldName))
            val project = projectGetter.invoke(null) as DSLProject
            project.perform()
        } catch (e: ClassNotFoundException) {
            failWithError("Class " + className + " was not found")
        } catch (e: NoSuchMethodException) {
            failWithError("Field " + fieldName + " in class " + className + " was not found")
        } catch (e: IllegalAccessException) {
            failWithError("Cannot get the specified object")
        } catch (e: InvocationTargetException) {
            failWithError("Cannot get the specified object")
        }
    }
}
