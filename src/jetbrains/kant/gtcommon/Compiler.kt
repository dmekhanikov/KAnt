package jetbrains.kant.gtcommon

import java.io.File
import jetbrains.kant.gtcommon.constants.KOTLIN_RUNTIME_JAR
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler

fun compileKotlinCode(classpath: String, output: String, vararg src: String) {
    val compiler = K2JVMCompiler()
    compiler.exec(System.out, "-classpath", classpath + File.pathSeparator + KOTLIN_RUNTIME_JAR, "-d", output,
            *src)
}
