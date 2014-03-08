package ru.ifmo.rain.mekhanikov.antdsl.example

import java.io.File
import kotlin.properties.Delegates

import org.apache.tools.ant.types.Path
import org.apache.tools.ant.types.Path.PathElement
import org.apache.tools.ant.Project
import org.apache.tools.ant.taskdefs.Javac

import ru.ifmo.rain.mekhanikov.antdsl.DSLElement
import ru.ifmo.rain.mekhanikov.antdsl.DSLTarget

/*class DSLPathElement : DSLElement() {
    var path: String by Delegates.mapVar(attributes)
    var location: String by Delegates.mapVar(attributes)

    override fun antObject(parent: Any?): Any {
        val pathElement = (parent as Path?)!!.createPathElement()
        pathElement?.setPath(path)
        if (attributes.containsKey("location")) {
            pathElement?.setLocation(File(location))
        }
        return pathElement!!
    }
}

abstract class DSLPathLikeElement() : DSLElement() {
    var path: String by Delegates.mapVar(attributes)
    var location: String by Delegates.mapVar(attributes)
    fun pathelement(init: DSLPathElement.() -> Unit) = initElement(DSLPathElement(), init)

    override fun antObject(parent: Any?) : Any {
        val pathObject = Path(parent as Project?, path)
        if (attributes.containsKey("location")) {
            pathObject.setLocation(File(location))
        }
        for (c in children) {
            pathObject.add(c.antObject(pathObject) as PathElement)
        }
        return pathObject
    }
}

class DSLSrc : DSLPathLikeElement()
class DSLClasspath : DSLPathLikeElement()
class DSLSourcepath : DSLPathLikeElement()
class DSLBootclasspath : DSLPathLikeElement()
class DSLExtdirs : DSLPathLikeElement()

class DSLCompilerarg : DSLElement() {
    var value: String by Delegates.mapVar(attributes)
    var line: String by Delegates.mapVar(attributes)
    var file: String by Delegates.mapVar(attributes)
    var path: String by Delegates.mapVar(attributes)
    var preffix: String by Delegates.mapVar(attributes)
    var suffix: String by Delegates.mapVar(attributes)
    var compiler: String by Delegates.mapVar(attributes)

    override fun antObject(parent: Any?): Any {
        val compilerArgObject = (parent as Javac?)!!.createCompilerArg()
        if (attributes.containsKey("value")) {
            compilerArgObject?.setValue(value)
        }
        if (attributes.containsKey("line")) {
            compilerArgObject?.setLine(line)
        }
        if (attributes.containsKey("file")) {
            compilerArgObject?.setFile(File(file))
        }
        if (attributes.containsKey("path")) {
            compilerArgObject?.setPath(Path((parent as Javac?)?.getProject(), path))
        }
        if (attributes.containsKey("preffix")) {
            compilerArgObject?.setPrefix(preffix)
        }
        if (attributes.containsKey("suffix")) {
            compilerArgObject?.setSuffix(suffix)
        }
        if (attributes.containsKey("compiler")) {
            compilerArgObject?.setCompiler(compiler)
        }
        return compilerArgObject!!
    }
}*/

class DSLJavac : DSLElement("javac") {
    var srcdir: String by Delegates.mapVar(attributes)
    var destdir: String by Delegates.mapVar(attributes)
    var classpath: String by Delegates.mapVar(attributes)
    var debug: Boolean by Delegates.mapVar(attributes)
    var source: String by Delegates.mapVar(attributes)

    /*fun src(init: DSLSrc.() -> Unit) : DSLSrc = initElement(DSLSrc(), init)
    fun classpath(init: DSLClasspath.() -> Unit) : DSLClasspath = initElement(DSLClasspath(), init)
    fun sourcepath(init: DSLSourcepath.() -> Unit) : DSLSourcepath = initElement(DSLSourcepath(), init)
    fun bootclasspath(init: DSLBootclasspath.() -> Unit) : DSLBootclasspath = initElement(DSLBootclasspath(), init)
    fun extdirs(init: DSLExtdirs.() -> Unit) : DSLExtdirs = initElement(DSLExtdirs(), init)
    fun compilerarg(init: DSLCompilerarg.() -> Unit) : DSLCompilerarg = initElement(DSLCompilerarg(), init)*/
}

fun DSLTarget.javac(init: DSLJavac.() -> Unit) : DSLJavac =
        initElement(DSLJavac(), init)