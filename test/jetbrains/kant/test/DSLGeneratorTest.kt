package jetbrains.kant.test

import jetbrains.kant.generator.DSLGenerator
import jetbrains.kant.constants.*
import jetbrains.kant.createClassLoader
import jetbrains.kant.cleanDirectory
import jetbrains.kant.compileKotlinCode
import java.lang.reflect.Method
import java.io.File
import java.io.File.pathSeparator
import jetbrains.kant.test.KAntTestCase.Property
import jetbrains.kant.getClassByPackage

var dslGeneratorTestInitComplete = false

class DSLGeneratorTest : KAntTestCase() {
    val GENERATOR_TEST_DATA_DIR = TEST_DATA_DIR + "DSLGenerator/"
    val GENERATOR_TEST_RES_DIR = TEST_RES_DIR + "DSLGenerator/"

    val runnerMainMethod: Method;
    {
        val dslDepends = array(ANT_JAR, ANT_LAUNCHER_JAR, ANT_CONTRIB_JAR, ARGS4J_JAR, KOTLIN_RUNTIME_JAR).join(pathSeparator)
        if (!dslGeneratorTestInitComplete) {
            File(DSL_GENERATED_DIR).cleanDirectory()
            DSLGenerator(DSL_SRC_ROOT, array(ANT_JAR, ANT_CONTRIB_JAR), array(), true, true).generate()
            File(DSL_BIN_DIR).cleanDirectory()
            compileKotlinCode(dslDepends, DSL_BIN_DIR, DSL_SRC_ROOT)
            dslGeneratorTestInitComplete = true
        }
        val classLoader = createClassLoader(DSL_BIN_DIR + pathSeparator + dslDepends)
        val packageClass = classLoader.loadClass(getClassByPackage(KANT_PACKAGE))!!
        runnerMainMethod = packageClass.getMethod("main", javaClass<Array<String>>())
    }

    private fun  runDSLGeneratorTest(testName: String, vararg properties: Property, check: () -> Boolean) {
        setProperties(properties)
        val fileName = GENERATOR_TEST_DATA_DIR + testName.capitalize() + ".kt"
        File(TEST_PLAYGROUND_BIN_DIR).cleanDirectory()
        compileKotlinCode(DSL_BIN_DIR, TEST_PLAYGROUND_BIN_DIR, fileName)
        File(TEST_PLAYGROUND_WORK_DIR).cleanDirectory()
        val projectName = testName + "Project"
        runnerMainMethod.invoke(null, array("-cp", TEST_PLAYGROUND_BIN_DIR, projectName))
        assert(check())
        clearProperties(properties)
    }

    public fun testMkdir() {
        val dir = File(TEST_PLAYGROUND_WORK_DIR + "temp/")
        runDSLGeneratorTest(
                "mkdir",
                Property("dir", dir.toString())
        ) { dir.exists() }
    }

    public fun testZipUnzip() {
        val toTarDir = File(GENERATOR_TEST_RES_DIR + "toZip/")
        val toTarFile = File(toTarDir.toString() + "/toZip.txt")
        val destFile = File(TEST_PLAYGROUND_WORK_DIR + "toUnzip.tar")
        val resDir = File(TEST_PLAYGROUND_WORK_DIR)
        val resFile = File(resDir.toString() + "/toZip.txt")
        runDSLGeneratorTest(
                "zipUnzip",
                Property("sourceDir", toTarDir.toString()),
                Property("zipFile", destFile.toString()),
                Property("outDir", resDir.toString())
        ) { assertFilesMatch(toTarFile, resFile); true }
    }

    public fun testCopy() {
        val srcDir = File(GENERATOR_TEST_RES_DIR + "toCopy/")
        val srcFile = File(srcDir.toString() + "/toCopy.txt")
        val destDir = File(TEST_PLAYGROUND_WORK_DIR)
        val resFile = File(destDir.toString() + "/toCopy.txt")
        runDSLGeneratorTest(
                "copy",
                Property("srcDir", srcDir.toString()),
                Property("destDir", destDir.toString())
        ) { assertFilesMatch(srcFile, resFile); true }
    }

    public fun testProperties() {
        val systemPropertiesOutFile = File(TEST_PLAYGROUND_WORK_DIR + "system.txt")
        val userPropertiesOutFile = File(TEST_PLAYGROUND_WORK_DIR + "user.txt")
        val propertiesResDir = File(GENERATOR_TEST_RES_DIR + "properties/")
        val propertiesFile = File(propertiesResDir.toString() + "/manifest.properties")
        val userExpFile = File(propertiesResDir.toString() + "/user.txt")
        runDSLGeneratorTest(
                "properties",
                Property("propertiesFile", propertiesFile.toString()),
                Property("systemPropertiesOutFile", systemPropertiesOutFile.toString()),
                Property("userPropertiesOutFile", userPropertiesOutFile.toString()),
                Property("stringProperty", "passed value"),
                Property("intProperty", "42"),
                Property("booleanProperty", "true"),
                Property("doubleProperty", "21568.3")
        ) {
            assertNotEmpty(systemPropertiesOutFile)
            assertFilesMatch(userExpFile, userPropertiesOutFile)
            true
        }
    }

    public fun testDepends() {
        val src1Dir = File(GENERATOR_TEST_RES_DIR + "toCopy/src1")
        val src1File = File(src1Dir.toString() + "/test1.txt")
        val src2Dir = File(GENERATOR_TEST_RES_DIR + "toCopy/src2")
        val src2File = File(src2Dir.toString() + "/test2.txt")
        val srcDir = File(TEST_PLAYGROUND_WORK_DIR + "src")
        val destDir = File(TEST_PLAYGROUND_WORK_DIR + "dest")
        val dest1File = File(destDir.toString() + "/test1.txt")
        val dest2File = File(destDir.toString() + "/test2.txt")
        runDSLGeneratorTest(
                "dependencies",
                Property("src1Dir", src1Dir.toString()),
                Property("src2Dir", src2Dir.toString()),
                Property("srcDir", srcDir.toString()),
                Property("destDir", destDir.toString())
        ) {
            assertFilesMatch(src1File, dest1File)
            assertFilesMatch(src2File, dest2File)
            true
        }
    }

    public fun testMath() {
        val destFile = File(TEST_PLAYGROUND_WORK_DIR + "out.txt")
        val expFile = File(GENERATOR_TEST_RES_DIR + "math/out.txt")
        runDSLGeneratorTest(
                "math",
                Property("destFile", destFile.toString()),
                Property("antContribJarFile", ANT_CONTRIB_JAR)
        ) { assertFilesMatch(expFile, destFile); true }
    }

    public fun testSwitch() {
        val destFile = File(TEST_PLAYGROUND_WORK_DIR + "out.txt")
        val expFile = File(GENERATOR_TEST_RES_DIR + "switch/out.txt")
        runDSLGeneratorTest(
                "switch",
                Property("destFile", destFile.toString()),
                Property("value", "bar"),
                Property("antContribJarFile", ANT_CONTRIB_JAR)
        ) { assertFilesMatch(expFile, destFile); true }
    }

    public fun testReplace() {
        val file = File(TEST_PLAYGROUND_WORK_DIR + "out.txt")
        val expFile = File(GENERATOR_TEST_RES_DIR + "replace/out.txt")
        runDSLGeneratorTest(
                "replace",
                Property("file", file.toString())
        ) { assertFilesMatch(expFile, file); true }
    }

    public fun testCutDirsMapper() {
        val srcDir = File(GENERATOR_TEST_RES_DIR + "toCopyAndCut/")
        val destDir = File(TEST_PLAYGROUND_WORK_DIR + "dest")
        val expFile = File(srcDir.toString() + "/foo/bar/toCopy.txt")
        val actFile = File(destDir.toString() + "/toCopy.txt")
        runDSLGeneratorTest(
                "cutDirsMapper",
                Property("srcDir", srcDir.toString()),
                Property("destDir", destDir.toString())
        ) { assertFilesMatch(expFile, actFile); true }
    }

    public fun testConditions() {
        val expFile = File(GENERATOR_TEST_RES_DIR + "conditions/out.txt")
        val actFile = File(TEST_PLAYGROUND_WORK_DIR + "/toCopy.txt")
        runDSLGeneratorTest(
                "conditions",
                Property("file", actFile.toString()),
                Property("booleanProperty", "true"),
                Property("string", "Hello, World!"),
                Property("pattern", ".*World!"),
                Property("arg1", "15"),
                Property("arg2", "fifteen")
        ) { assertFilesMatch(expFile, actFile); true }
    }
}
