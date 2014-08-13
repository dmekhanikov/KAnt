package jetbrains.kant.test

import jetbrains.kant.generator.DSLGenerator
import jetbrains.kant.generator.DSL_ROOT
import jetbrains.kant.*
import java.lang.reflect.Method
import java.io.File
import java.io.File.pathSeparator
import jetbrains.kant.test.KAntTestCase.Property

var dslGeneratorTestInitComplete = false

class DSLGeneratorTest : KAntTestCase() {
    val ANT_LAUNCHER_JAR_FILE = "lib/ant-launcher-1.9.4.jar"
    val ANT_CONTRIB_JAR_FILE = "lib/ant-contrib-1.0b3.jar"
    val DSL_GENERATOR_OUT_ROOT = TEST_OUT_ROOT + "DSLGenrator/"
    val DSL_GENERATOR_TEST_DATA = TEST_DATA_ROOT + "DSLGenerator/"
    val DSL_GENERATOR_TEST_RES = TEST_RES_ROOT + "DSLGenerator/"
    val DSL_GENERATOR_TEST_PACKAGE = "testData.DSLGenerator"
    val DSL_GENERATED_ROOT = DSL_ROOT + "jetbrains/kant/dsl/generated/"
    val WORKING_DIR = DSL_GENERATOR_OUT_ROOT + "playground/"

    val classLoader = createClassLoader(array(DSL_GENERATOR_OUT_ROOT,
            ANT_JAR_FILE, ANT_LAUNCHER_JAR_FILE, KOTLIN_RUNTIME_JAR_FILE));
    {
        if (!dslGeneratorTestInitComplete) {
            File(DSL_GENERATED_ROOT).cleanDirectory()
            DSLGenerator(DSL_ROOT, array(ANT_JAR_FILE, ANT_CONTRIB_JAR_FILE), array(), true, true).generate()
            File(DSL_GENERATOR_OUT_ROOT).cleanDirectory()
            compileKotlinCode(DSL_ROOT + pathSeparator + DSL_GENERATOR_TEST_DATA, ANT_JAR_FILE,
                    DSL_GENERATOR_OUT_ROOT)
            dslGeneratorTestInitComplete = true
        }
    }

    private fun getMainMethod(packageName: String): Method {
        val fullPackageName = DSL_GENERATOR_TEST_PACKAGE + "." + packageName + "." + packageName.capitalize() + "Package"
        val packageClass = classLoader.loadClass(fullPackageName)!!
        return packageClass.getMethod("main", javaClass<Array<String>?>())
    }

    private fun runDSLGeneratorTest(packageName: String, properties: Array<Property>?,
                                    init: () -> Boolean, check: () -> Boolean) {
        val mainMethod = getMainMethod(packageName)
        setProperties(properties)
        assert(init())
        mainMethod.invoke(null, null)
        assert(check())
        clearProperties(properties)
    }

    public fun testMkdir() {
        val dir = File(WORKING_DIR + "temp/")
        runDSLGeneratorTest(
                "mkdir",
                array(Property("dir", dir.toString())),
                { File(WORKING_DIR).cleanDirectory(); true },
                { dir.exists() }
        )
    }

    public fun testZipUnzip() {
        val toTarDir = File(DSL_GENERATOR_TEST_RES + "toZip/")
        val toTarFile = File(toTarDir.toString() + "/toZip.txt")
        val destFile = File(WORKING_DIR + "toUnzip.tar")
        val resDir = File(WORKING_DIR)
        val resFile = File(resDir.toString() + "/toZip.txt")
        runDSLGeneratorTest(
                "zipUnzip",
                array(Property("sourceDir", toTarDir.toString()),
                      Property("zipFile", destFile.toString()),
                      Property("outDir", resDir.toString())),
                { File(WORKING_DIR).cleanDirectory(); true },
                { assertFilesMatch(toTarFile, resFile); true }
        )
    }

    public fun testCopy() {
        val srcDir = File(DSL_GENERATOR_TEST_RES + "toCopy/")
        val srcFile = File(srcDir.toString() + "/toCopy.txt")
        val destDir = File(WORKING_DIR)
        val resFile = File(destDir.toString() + "/toCopy.txt")
        runDSLGeneratorTest(
                "copy",
                array(Property("srcDir", srcDir.toString()),
                      Property("destDir", destDir.toString())),
                { File(WORKING_DIR).cleanDirectory(); true },
                { assertFilesMatch(srcFile, resFile); true }
        )
    }

    public fun testProperties() {
        val systemPropertiesOutFile = File(WORKING_DIR + "system.txt")
        val userPropertiesOutFile = File(WORKING_DIR + "user.txt")
        val propertiesResDir = File(DSL_GENERATOR_TEST_RES + "properties/")
        val propertiesFile = File(propertiesResDir.toString() + "/manifest.properties")
        val userExpFile = File(propertiesResDir.toString() + "/user.txt")
        runDSLGeneratorTest(
                "properties",
                array(Property("propertiesFile", propertiesFile.toString()),
                      Property("systemPropertiesOutFile", systemPropertiesOutFile.toString()),
                      Property("userPropertiesOutFile", userPropertiesOutFile.toString()),
                      Property("stringProperty", "passed value"),
                      Property("intProperty", "42"),
                      Property("booleanProperty", "true"),
                      Property("doubleProperty", "21568.3")),
                { File(WORKING_DIR).cleanDirectory(); true },
                {
                    assertNotEmpty(systemPropertiesOutFile)
                    assertFilesMatch(userExpFile, userPropertiesOutFile)
                    true
                }
        )
    }

    public fun testDepends() {
        val src1Dir = File(DSL_GENERATOR_TEST_RES + "toCopy/src1")
        val src1File = File(src1Dir.toString() + "/test1.txt")
        val src2Dir = File(DSL_GENERATOR_TEST_RES + "toCopy/src2")
        val src2File = File(src2Dir.toString() + "/test2.txt")
        val srcDir = File(WORKING_DIR + "src")
        val destDir = File(WORKING_DIR + "dest")
        val dest1File = File(destDir.toString() + "/test1.txt")
        val dest2File = File(destDir.toString() + "/test2.txt")
        runDSLGeneratorTest(
                "dependencies",
                array(Property("src1Dir", src1Dir.toString()),
                      Property("src2Dir", src2Dir.toString()),
                      Property("srcDir", srcDir.toString()),
                      Property("destDir", destDir.toString())),
                { File(WORKING_DIR).cleanDirectory(); true },
                {
                    assertFilesMatch(src1File, dest1File)
                    assertFilesMatch(src2File, dest2File)
                    true
                }
        )
    }

    public fun testMath() {
        val destFile = File(WORKING_DIR + "out.txt")
        val expFile = File(DSL_GENERATOR_TEST_RES + "math/out.txt")
        runDSLGeneratorTest(
                "math",
                array(Property("destFile", destFile.toString()),
                      Property("antContribJarFile", ANT_CONTRIB_JAR_FILE)),
                { File(WORKING_DIR).cleanDirectory(); true },
                { assertFilesMatch(expFile, destFile); true }
        )
    }

    public fun testSwitch() {
        val destFile = File(WORKING_DIR + "out.txt")
        val expFile = File(DSL_GENERATOR_TEST_RES + "switch/out.txt")
        runDSLGeneratorTest(
                "switch",
                array(Property("destFile", destFile.toString()),
                      Property("value", "bar"),
                      Property("antContribJarFile", ANT_CONTRIB_JAR_FILE)),
                { File(WORKING_DIR).cleanDirectory(); true },
                { assertFilesMatch(expFile, destFile); true }
        )
    }

    public fun testReplace() {
        val file = File(WORKING_DIR + "out.txt")
        val expFile = File(DSL_GENERATOR_TEST_RES + "replace/out.txt")
        runDSLGeneratorTest(
                "replace",
                array(Property("file", file.toString())),
                { File(WORKING_DIR).cleanDirectory(); true },
                { assertFilesMatch(expFile, file); true }
        )
    }

    public fun testCutDirsMapper() {
        val srcDir = File(DSL_GENERATOR_TEST_RES + "toCopyAndCut/")
        val destDir = File(WORKING_DIR + "dest")
        val expFile = File(srcDir.toString() + "/foo/bar/toCopy.txt")
        val actFile = File(destDir.toString() + "/toCopy.txt")
        runDSLGeneratorTest(
                "cutdirsmapper",
                array(Property("srcDir", srcDir.toString()),
                      Property("destDir", destDir.toString())),
                { File(WORKING_DIR).cleanDirectory(); true },
                { assertFilesMatch(expFile, actFile); true }
        )
    }

    public fun testConditions() {
        val expFile = File(DSL_GENERATOR_TEST_RES + "conditions/out.txt")
        val actFile = File(WORKING_DIR + "/toCopy.txt")
        runDSLGeneratorTest(
                "conditions",
                array(Property("file", actFile.toString()),
                      Property("booleanProperty", "true"),
                      Property("string", "Hello, World!"),
                      Property("pattern", ".*World!"),
                      Property("arg1", "15"),
                      Property("arg2", "fifteen")),
                { File(WORKING_DIR).cleanDirectory(); true },
                { assertFilesMatch(expFile, actFile); true }
        )
    }
}
