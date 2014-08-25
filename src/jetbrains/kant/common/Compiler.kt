package jetbrains.kant.gtcommon

import org.jetbrains.jet.cli.jvm.K2JVMCompiler
import java.io.File
import jetbrains.kant.gtcommon.constants.KOTLIN_RUNTIME_JAR

public fun compileKotlinCode(classpath: String, output: String, vararg src: String) {
    val compiler = K2JVMCompiler()
    compiler.exec(System.out, "-classpath", classpath + File.pathSeparator + KOTLIN_RUNTIME_JAR, "-d", output,
            *(src as Array<String?>))
}
