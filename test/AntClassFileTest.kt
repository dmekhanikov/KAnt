import kotlin.test.assertEquals
import java.io.FileInputStream
import java.io.File
import java.io.Writer
import java.io.FileWriter
import ru.ifmo.rain.mekhanikov.ant2kotlin.ClassFile
import ru.ifmo.rain.mekhanikov.ant2kotlin.AntClassFile

class AntClassFileTest : Ant2KotlinTestCase() {

    val ANT_CLASS_FILE_TEST_DATA_DIR = TEST_DATA_ROOT + "AntClassFile/"
    val ANT_CLASS_FILE_TEST_EXP_DIR = TEST_DATA_ROOT + "AntClassFile/exp/"
    val ANT_CLASS_FILE_TEST_ACT_DIR = TEST_DATA_OUT_ROOT + "AntClassFile/act/";
    {
        createDirectory(ANT_CLASS_FILE_TEST_ACT_DIR)
    }

    private fun testToKotlin(name : String) {
        val inFile = File(ANT_CLASS_FILE_TEST_DATA_DIR + name + ".class")
        val expOutFile = File(ANT_CLASS_FILE_TEST_EXP_DIR + name + ".exp")
        val actOutFile = File(ANT_CLASS_FILE_TEST_ACT_DIR + name + ".act")
        val antClassFile = AntClassFile(FileInputStream(inFile))
        val outWriter = FileWriter(actOutFile)

        outWriter.write(antClassFile.toKotlin())
        outWriter.close()
        assertFilesMatch(expOutFile, actOutFile)
    }

    public fun testJavac() {
        testToKotlin("Javac")
    }
}