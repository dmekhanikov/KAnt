package ru.ifmo.rain.mekhanikov.ant2kotlin.dslExample

import java.io.File

import org.apache.tools.ant.types.Path
import org.apache.tools.ant.types.Path.PathElement
import org.apache.tools.ant.Project
import org.apache.tools.ant.taskdefs.Javac

import ru.ifmo.rain.mekhanikov.ant2kotlin.DSLElement
import ru.ifmo.rain.mekhanikov.ant2kotlin.DSLAttributeAdapter

class DSLPathElement : DSLElement("pathelement") {
    var path: String? by DSLAttributeAdapter()
    var location: String? by DSLAttributeAdapter()

    override fun antObject(parent: Any?): Any {
        val pathElement = (parent as Path?)!!.createPathElement()
        pathElement?.setPath(path)
        if (location != null) {
            pathElement?.setLocation(File(location!!))
        }
        return pathElement!!
    }
}

abstract class DSLPathLikeElement(elementName: String) : DSLElement(elementName) {
    var path: String? by DSLAttributeAdapter()
    var location: String? by DSLAttributeAdapter()
    fun pathelement(init: DSLPathElement.() -> Unit) = initElement(DSLPathElement(), init)

    override fun antObject(parent: Any?) : Any {
        val pathObject = Path(parent as Project?, path)
        if (location != null) {
            pathObject.setLocation(File(location!!))
        }
        for (c in children) {
            pathObject.add(c.antObject(pathObject) as PathElement)
        }
        return pathObject
    }
}

class DSLSrc : DSLPathLikeElement("src")
class DSLClasspath : DSLPathLikeElement("classpath")
class DSLSourcepath : DSLPathLikeElement("sourcepath")
class DSLBootclasspath : DSLPathLikeElement("bootclasspath")
class DSLExtdirs : DSLPathLikeElement("extdirs")

class DSLCompilerarg : DSLElement("compilerarg") {
    var value: String? by DSLAttributeAdapter()
    var line: String? by DSLAttributeAdapter()
    var file: String? by DSLAttributeAdapter()
    var path: String? by DSLAttributeAdapter()
    var preffix: String? by DSLAttributeAdapter()
    var suffix: String? by DSLAttributeAdapter()
    var compiler: String? by DSLAttributeAdapter()

    override fun antObject(parent: Any?): Any {
        val compilerArgObject = (parent as Javac?)!!.createCompilerArg()
        if (value != null) {
            compilerArgObject?.setValue(value)
        }
        if (line != null) {
            compilerArgObject?.setLine(line)
        }
        if (file != null) {
            compilerArgObject?.setFile(File(file!!))
        }
        if (path != null) {
            compilerArgObject?.setPath(Path((parent as Javac?)?.getProject(), path))
        }
        if (preffix != null) {
            compilerArgObject?.setPrefix(preffix)
        }
        if (suffix != null) {
            compilerArgObject?.setSuffix(suffix)
        }
        if (compiler != null) {
            compilerArgObject?.setCompiler(compiler)
        }
        return compilerArgObject!!
    }
}

class DSLJavac : DSLElement("javac") {
    var srcdir: String? by DSLAttributeAdapter()
    var destdir: String? by DSLAttributeAdapter()
    var classpath: String? by DSLAttributeAdapter()
    var debug: String? by DSLAttributeAdapter()
    var source: String? by DSLAttributeAdapter()

    fun src(init: DSLSrc.() -> Unit) : DSLSrc = initElement(DSLSrc(), init)
    fun classpath(init: DSLClasspath.() -> Unit) : DSLClasspath = initElement(DSLClasspath(), init)
    fun sourcepath(init: DSLSourcepath.() -> Unit) : DSLSourcepath = initElement(DSLSourcepath(), init)
    fun bootclasspath(init: DSLBootclasspath.() -> Unit) : DSLBootclasspath = initElement(DSLBootclasspath(), init)
    fun extdirs(init: DSLExtdirs.() -> Unit) : DSLExtdirs = initElement(DSLExtdirs(), init)
    fun compilerarg(init: DSLCompilerarg.() -> Unit) : DSLCompilerarg = initElement(DSLCompilerarg(), init)

    override fun antObject(parent: Any?): Any {
        val project = parent as Project?
        val javacObject = Javac()
        javacObject.setProject(project)
        javacObject.setTaskType("javac")
        javacObject.setTaskName("javac")
        if (srcdir != null) {
            javacObject.setSrcdir(Path(project, srcdir))
        }
        if (destdir != null) {
            javacObject.setDestdir(File(destdir!!))
        }
        if (classpath != null) {
            javacObject.setClasspath(Path(project, classpath))
        }
        if (debug != null) {
            javacObject.setDebug(debug!!.toLowerCase().equals("true"))
        }
        if (source != null) {
            javacObject.setSource(source)
        }
        return javacObject
    }

    fun execute() {
        val project = Project()
        project.init()
        val javacObject = antObject(project) as Javac
        javacObject.execute()
    }
}

fun javac(init: DSLJavac.() -> Unit) : DSLJavac {
    val javac = DSLJavac()
    javac.init()
    javac.execute()
    return javac
}