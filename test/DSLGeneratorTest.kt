import java.io.File
import ru.ifmo.rain.mekhanikov.ant2kotlin.DSLGenerator
import ru.ifmo.rain.mekhanikov.compileKotlinCode
import ru.ifmo.rain.mekhanikov.cleanDirectory
import ru.ifmo.rain.mekhanikov.createClassLoader
import java.lang.reflect.Method
import ru.ifmo.rain.mekhanikov.deleteRecursively

var dslGeneratorTestInitComplete = false

class DSLGeneratorTest : Ant2KotlinTestCase() {
    val ANT_JAR_FILE = "lib/ant-1.9.3.jar"
    val ANT_LAUNCHER_JAR_FILE = "lib/ant-launcher-1.9.3.jar"
    val KOTLIN_RUNTIME_JAR_FILE = "lib/kotlin-runtime.jar"
    val DSL_GENERATOR_OUT_ROOT = TEST_OUT_ROOT + "DSLGenerator/"
    val DSL_GENERATOR_TEST_DATA = TEST_DATA_ROOT + "DSLGenerator/"
    val DSL_GENERATOR_TEST_RES = TEST_RES_ROOT + "DSLGenerator/"
    val DSL_GENERATOR_TEST_PACKAGE = "testData.DSLGenerator"
    val DSL_ROOT = "dsl/src/"
    val DSL_GENERATED_ROOT = DSL_ROOT + "ru/ifmo/rain/mekhanikov/antdsl/generated/"
    val WORKING_DIR = DSL_GENERATOR_OUT_ROOT + "playground/"

    val classLoader = createClassLoader(DSL_GENERATOR_OUT_ROOT,
            ANT_JAR_FILE, ANT_LAUNCHER_JAR_FILE, KOTLIN_RUNTIME_JAR_FILE);
    {
        if (!dslGeneratorTestInitComplete) {
            File(DSL_GENERATED_ROOT).cleanDirectory()
            DSLGenerator(ANT_JAR_FILE, DSL_ROOT).generate()
            File(DSL_GENERATOR_OUT_ROOT).cleanDirectory()
            compileKotlinCode(DSL_ROOT + ":" + DSL_GENERATOR_TEST_DATA,
                    ANT_JAR_FILE + ":" + KOTLIN_RUNTIME_JAR_FILE,
                    DSL_GENERATOR_OUT_ROOT)
            dslGeneratorTestInitComplete = true
        }
    }

    private fun getMainMethod(packageName : String): Method {
        val fullPackageName = DSL_GENERATOR_TEST_PACKAGE + "." + packageName + "." + packageName.capitalize() + "Package"
        val packageClass = classLoader.loadClass(fullPackageName)!!
        return packageClass.getMethod("main", javaClass<Array<String>?>())
    }

    private fun runDSLGeneratorTest(packageName : String, args : Array<String>?,
                                    init : () -> Boolean, check : () -> Boolean) {
        val mainMethod = getMainMethod(packageName)
        assert(init())
        mainMethod.invoke(null, args)
        assert(check())
    }

    public fun testMkdir() {
        val dir = File(WORKING_DIR + "temp/")
        runDSLGeneratorTest(
                "mkdir",
                array(dir.toString()),
                { File(WORKING_DIR).cleanDirectory(); true },
                { dir.exists() }
        )
    }

    public fun testTarUntar() {
        val toTarDir = File(DSL_GENERATOR_TEST_RES + "toTar/")
        val toTarFile = File(toTarDir.toString() + "/toTar.txt")
        val destFile = File(WORKING_DIR + "toUntar.tar")
        val resDir = File(WORKING_DIR)
        val resFile = File(resDir.toString() + "/toTar.txt")
        runDSLGeneratorTest(
                "tarUntar",
                array(toTarDir.toString(), destFile.toString(), resDir.toString()),
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
                array(srcDir.toString(), destDir.toString()),
                { File(WORKING_DIR).cleanDirectory(); true },
                { assertFilesMatch(srcFile, resFile); true }
        )
    }
}
