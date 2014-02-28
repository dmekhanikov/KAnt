import java.io.File
import java.io.FileWriter
import ru.ifmo.rain.mekhanikov.ant2kotlin.AntClass
import ru.ifmo.rain.mekhanikov.ant2kotlin.createClassLoader

class AntClassTest : Ant2KotlinTestCase() {

    val ANT_CLASS_FILE_TEST_DATA_DIR = "lib/ant-1.9.3.jar"
    val ANT_CLASS_FILE_TEST_EXP_DIR = TEST_DATA_ROOT + "AntClassFile/exp/"
    val ANT_CLASS_FILE_TEST_ACT_DIR = TEST_DATA_OUT_ROOT + "AntClassFile/act/";
    {
        createDirectory(ANT_CLASS_FILE_TEST_ACT_DIR)
    }

    private fun testAttributes(className: String) {
        val classPath = ANT_CLASS_FILE_TEST_DATA_DIR
        val splitClassName = className.split('.')
        val fileName = splitClassName[splitClassName.size - 1]
        val expOutFile = File(ANT_CLASS_FILE_TEST_EXP_DIR + fileName + ".exp")
        val actOutFile = File(ANT_CLASS_FILE_TEST_ACT_DIR + fileName + ".act")
        val antClassFile = AntClass(createClassLoader(classPath), className)
        val outWriter = FileWriter(actOutFile)

        outWriter.write(antClassFile.toKotlin().toString())
        outWriter.close()
        assertFilesMatch(expOutFile, actOutFile)
    }

    public fun testJavac() {
        testAttributes("org.apache.tools.ant.taskdefs.Javac")
    }

    public fun testMkdir() {
        testAttributes("org.apache.tools.ant.taskdefs.Mkdir")
    }
}