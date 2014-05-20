import ru.ifmo.rain.mekhanikov.ant2kotlin.DSLGenerator
import ru.ifmo.rain.mekhanikov.compileKotlinCode
import ru.ifmo.rain.mekhanikov.cleanDirectory
import ru.ifmo.rain.mekhanikov.createClassLoader
import ru.ifmo.rain.mekhanikov.ant2kotlin.DSL_ROOT
import ru.ifmo.rain.mekhanikov.KOTLIN_RUNTIME_JAR_FILE
import ru.ifmo.rain.mekhanikov.ANT_JAR_FILE
import java.lang.reflect.Method
import java.io.File

var dslGeneratorTestInitComplete = false

class DSLGeneratorTest : Ant2KotlinTestCase() {
    val ANT_LAUNCHER_JAR_FILE = "lib/ant-launcher-1.9.3.jar"
    val ANT_CONTRIB_JAR_FILE = "lib/ant-contrib-1.0b3.jar"
    val DSL_GENERATOR_OUT_ROOT = TEST_OUT_ROOT + "DSLGenrator/"
    val DSL_GENERATOR_TEST_DATA = TEST_DATA_ROOT + "DSLGenerator/"
    val DSL_GENERATOR_TEST_RES = TEST_RES_ROOT + "DSLGenerator/"
    val DSL_GENERATOR_TEST_PACKAGE = "testData.DSLGenerator"
    val DSL_GENERATED_ROOT = DSL_ROOT + "ru/ifmo/rain/mekhanikov/antdsl/generated/"
    val WORKING_DIR = DSL_GENERATOR_OUT_ROOT + "playground/"

    val classLoader = createClassLoader(array(DSL_GENERATOR_OUT_ROOT,
            ANT_JAR_FILE, ANT_LAUNCHER_JAR_FILE, KOTLIN_RUNTIME_JAR_FILE));
    {
        if (!dslGeneratorTestInitComplete) {
            File(DSL_GENERATED_ROOT).cleanDirectory()
            DSLGenerator(DSL_ROOT, array(ANT_JAR_FILE, ANT_CONTRIB_JAR_FILE)).generate()
            File(DSL_GENERATOR_OUT_ROOT).cleanDirectory()
            compileKotlinCode(DSL_ROOT + ":" + DSL_GENERATOR_TEST_DATA,
                    "$ANT_JAR_FILE:$KOTLIN_RUNTIME_JAR_FILE",
                    DSL_GENERATOR_OUT_ROOT)
            dslGeneratorTestInitComplete = true
        }
    }

    private fun getMainMethod(packageName: String): Method {
        val fullPackageName = DSL_GENERATOR_TEST_PACKAGE + "." + packageName + "." + packageName.capitalize() + "Package"
        val packageClass = classLoader.loadClass(fullPackageName)!!
        return packageClass.getMethod("main", javaClass<Array<String>?>())
    }

    private fun runDSLGeneratorTest(packageName: String, args: Array<String>?,
                                    init: () -> Boolean, check: () -> Boolean) {
        val mainMethod = getMainMethod(packageName)
        assert(init())
        mainMethod.invoke(null, args)
        assert(check())
    }

    public fun testMkdir() {
        val dir = File(WORKING_DIR + "temp/")
        runDSLGeneratorTest(
                "mkdir",
                array("-Ddir=" + dir.toString()),
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
                array("-DsourceDir=" + toTarDir.toString(), "-DzipFile=" + destFile.toString(), "-DoutDir=" + resDir.toString()),
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
                array("-DsrcDir=" + srcDir.toString(), "-DdestDir=" + destDir.toString()),
                { File(WORKING_DIR).cleanDirectory(); true },
                { assertFilesMatch(srcFile, resFile); true }
        )
    }

    public fun testProperties() {
        val systemPropertiesOutFile = File(WORKING_DIR + "system.txt")
        val userPropertiesOutFile = File(WORKING_DIR + "user.txt")
        val propertiesResDir = File(DSL_GENERATOR_TEST_RES + "properties/")
        val userExpFile = File(propertiesResDir.toString() + "/user.txt")
        runDSLGeneratorTest(
                "properties",
                array("-DsystemPropertiesOutFile=" + systemPropertiesOutFile.toString(),
                        "-DuserPropertiesOutFile=" + userPropertiesOutFile.toString(),
                        "-DstringProperty=passed value",
                        "-DintProperty=42",
                        "-DbooleanProperty=true",
                        "-DdoubleProperty=21568.3"),
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
                array("-Dsrc1Dir=" + src1Dir.toString(),
                        "-Dsrc2Dir=" + src2Dir.toString(),
                        "-DsrcDir=" + srcDir.toString(),
                        "-DdestDir=" + destDir.toString()),
                { File(WORKING_DIR).cleanDirectory(); true },
                {
                    assertFilesMatch(src1File, dest1File)
                    assertFilesMatch(src2File, dest2File)
                    true
                }
        )
    }

    public fun testExternalLibraries() {
        val destFile = File(WORKING_DIR + "out.txt")
        val expFile = File(DSL_GENERATOR_TEST_RES + "switch/out.txt")
        runDSLGeneratorTest(
                "externalLibraries",
                array("-DdestFile=" + destFile.toString(),
                        "-Dvalue=bar",
                        "-DantContribJarFile=$ANT_CONTRIB_JAR_FILE"),
                { File(WORKING_DIR).cleanDirectory(); true },
                { assertFilesMatch(expFile, destFile); true }
        )
    }

    public fun testCutDirsMapper() {
        val srcDir = File(DSL_GENERATOR_TEST_RES + "toCopyAndCut/")
        val destDir = File(WORKING_DIR + "dest")
        val expFile = File(srcDir.toString() + "/foo/bar/toCopy.txt")
        val actFile = File(destDir.toString() + "/toCopy.txt")
        runDSLGeneratorTest(
                "cutdirsmapper",
                array("-DsrcDir=" + srcDir.toString(),
                        "-DdestDir=" + destDir.toString()),
                { File(WORKING_DIR).cleanDirectory(); true },
                { assertFilesMatch(expFile, actFile); true }
        )
    }
}
