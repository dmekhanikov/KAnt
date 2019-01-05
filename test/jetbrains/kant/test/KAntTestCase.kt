package jetbrains.kant.test

import jetbrains.kant.common.createClassLoader
import jetbrains.kant.generator.DSLGenerator
import jetbrains.kant.gtcommon.cleanDirectory
import jetbrains.kant.gtcommon.compileKotlinCode
import jetbrains.kant.gtcommon.constants.ANT_CONTRIB_JAR
import jetbrains.kant.gtcommon.constants.ANT_JAR
import junit.framework.TestCase
import java.io.File
import java.io.File.pathSeparator
import java.lang.reflect.InvocationTargetException

var testInitComplete = false

abstract class KAntTestCase : TestCase() {
    init {
        if (!testInitComplete) {
            testInitComplete = true
            File(GENERATOR_OUT_DIR).cleanDirectory()
            val generator = DSLGenerator(GENERATOR_OUT_DIR, ANT_JAR + pathSeparator + ANT_CONTRIB_JAR,
                    emptyArray(), true, true)
            generator.generate()
            generator.compile()
        }
    }

    fun runBoxTest(file: String) {
        preparePlayground()
        compileKotlinCode(DSL_BIN_DIR + pathSeparator + TEST_BIN_DIR + pathSeparator + GT_BIN_DIR, TEST_PLAYGROUND_BIN_DIR, file)
        val classLoader = createClassLoader(arrayOf(DSL_BIN_DIR, *DSL_DEPENDS,
                TEST_PLAYGROUND_BIN_DIR, TEST_BIN_DIR, GT_BIN_DIR), null)
        val packageClass = classLoader.loadClass("_DefaultPackage")!!
        val boxMethod = packageClass.getMethod("box")
        val result: Any?
        try {
            result = boxMethod.invoke(null)
        } catch (e: InvocationTargetException) {
            throw e.cause!!
        }
        assertEquals("OK", result)
    }

    fun preparePlayground() {
        File(TEST_PLAYGROUND_DIR).cleanDirectory()
        File(TEST_PLAYGROUND_BIN_DIR).mkdirs()
        File(TEST_PLAYGROUND_WORK_DIR).mkdirs()
    }
}
