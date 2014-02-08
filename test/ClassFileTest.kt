import kotlin.test.assertEquals
import java.io.FileInputStream
import java.io.File
import java.io.Writer
import java.io.FileWriter
import ru.ifmo.rain.mekhanikov.ant2kotlin.ClassFile
import ru.ifmo.rain.mekhanikov.ant2kotlin.AntClassFile

class ClassFileTest : Ant2KotlinTestCase() {

    val CLASS_FILE_TEST_OUT_DIR = TEST_DATA_OUT_ROOT + "ClassFile/"
    val CLASS_FILE_TEST_EXP_DIR = TEST_DATA_ROOT + "ClassFile/exp/"
    val CLASS_FILE_TEST_ACT_DIR = CLASS_FILE_TEST_OUT_DIR + "act/";
    {
        createDirectory(CLASS_FILE_TEST_ACT_DIR)
    }

    private fun dumpPublicMethods(classFile: ClassFile, writer: Writer) {
        for (method in classFile.publicMethods()) {
            writer.write(method.toString()!! + "\n")
        }
        writer.close()
    }

    private fun testPublicMethods(name : String) {
        val inFile = File(CLASS_FILE_TEST_OUT_DIR + name + ".class")
        val expOutFile = File(CLASS_FILE_TEST_EXP_DIR + name + ".exp")
        val actOutFile = File(CLASS_FILE_TEST_ACT_DIR + name + ".act")
        val classFile = ClassFile(FileInputStream(inFile))
        dumpPublicMethods(classFile, FileWriter(actOutFile))
        assertFilesMatch(expOutFile, actOutFile)
    }

    public fun testSimpleClass() {
        testPublicMethods("SimpleClass")
    }

    public fun testClassWithOverrides() {
        testPublicMethods("ClassWithOverrides")
    }

    public fun testInterface() {
        testPublicMethods("Interface")
    }
}