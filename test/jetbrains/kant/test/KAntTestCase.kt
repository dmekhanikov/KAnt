package jetbrains.kant.test

import junit.framework.TestCase
import kotlin.test.assertEquals
import java.lang.reflect.InvocationTargetException
import java.io.File
import java.io.File.pathSeparator
import jetbrains.kant.gtcommon.compileKotlinCode
import jetbrains.kant.gtcommon.cleanDirectory
import jetbrains.kant.gtcommon.constants.ANT_JAR
import jetbrains.kant.gtcommon.constants.ANT_CONTRIB_JAR
import jetbrains.kant.gtcommon.constants.DSL_SRC_ROOT
import jetbrains.kant.gtcommon.constants.DSL_GENERATED_DIR
import jetbrains.kant.generator.DSLGenerator
import jetbrains.kant.common.createClassLoader

var testInitComplete = false

abstract class KAntTestCase : TestCase() {
    {
        if (!testInitComplete) {
            testInitComplete = true
            File(GENERATOR_OUT_DIR).cleanDirectory()
            val generator = DSLGenerator(GENERATOR_OUT_DIR, ANT_JAR + pathSeparator + ANT_CONTRIB_JAR, array(), true, true)
            generator.generate()
            generator.compile()
        }
    }

    fun runBoxTest(file: String) {
        preparePlayground()
        compileKotlinCode(DSL_BIN_DIR + pathSeparator + TEST_BIN_DIR + pathSeparator + GT_BIN_DIR, TEST_PLAYGROUND_BIN_DIR, file)
        val classLoader = createClassLoader(array(DSL_BIN_DIR, *DSL_DEPENDS,
                TEST_PLAYGROUND_BIN_DIR, TEST_BIN_DIR, GT_BIN_DIR), null)
        val packageClass = classLoader.loadClass("_DefaultPackage")!!
        val boxMethod = packageClass.getMethod("box")
        val result: Any?
        try {
            result = boxMethod.invoke(null)
        } catch(e: InvocationTargetException) {
            throw e.getCause()!!
        }
        assertEquals("OK", result)
    }

    fun preparePlayground() {
        File(TEST_PLAYGROUND_DIR).cleanDirectory()
        File(TEST_PLAYGROUND_BIN_DIR).mkdirs()
        File(TEST_PLAYGROUND_WORK_DIR).mkdirs()
    }
}
