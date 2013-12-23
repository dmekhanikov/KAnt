import kotlin.test.assertEquals
import java.io.FileInputStream
import java.io.File
import java.io.Writer
import java.io.FileWriter
import ru.ifmo.rain.mekhanikov.ant2kotlin.simpleParser.ClassFile

class ClassFileTests : Ant2KotlinTestCase() {

    val CLASS_FILE_TEST_RES_DIR = TEST_DATA_OUT_ROOT + "ClassFile/"
    val CLASS_FILE_TEST_OUT_DIR = TEST_DATA_ROOT + "ClassFile/out/"
    val CLASS_FILE_TEST_ACT_OUT_DIR = CLASS_FILE_TEST_RES_DIR + "out/";
    {
        val dir = File(CLASS_FILE_TEST_ACT_OUT_DIR)
        if (!dir.exists()) {
            dir.mkdir()
        }
    }

    private fun dumpMethods(classFile: ClassFile, writer: Writer) {
        for (method in classFile.methods()) {
            writer.write(method.toString()!! + "\n")
        }
        writer.close()
    }

    private fun check(inFile: File, outFile : File, actOutFile: File) {
        val classFile = ClassFile(FileInputStream(inFile))
        if (!outFile.exists()) {
            dumpMethods(classFile, FileWriter(actOutFile))
            assert(false)
        } else {
            dumpMethods(classFile, FileWriter(actOutFile))
            if (!filesDiffer(outFile, actOutFile)) {
                assert(false)
            } else {
                actOutFile.delete()
            }
        }
    }

    public fun testSimpleClass() {
        val inFile = File(CLASS_FILE_TEST_RES_DIR + "SimpleClass.class")
        val outFile = File(CLASS_FILE_TEST_OUT_DIR + "SimpleClass.out")
        val actOutFile = File(CLASS_FILE_TEST_ACT_OUT_DIR + "SimpleClass.out.act")
        check(inFile, outFile, actOutFile)
    }

    public fun testClassWithOverrides() {
        val inFile = File(CLASS_FILE_TEST_RES_DIR + "ClassWithOverrides.class")
        val outFile = File(CLASS_FILE_TEST_OUT_DIR + "ClassWithOverrides.out")
        val actOutFile = File(CLASS_FILE_TEST_ACT_OUT_DIR + "ClassWithOverrides.out.act")
        check(inFile, outFile, actOutFile)
    }

    public fun testClassWithStaticInit() {
        val inFile = File(CLASS_FILE_TEST_RES_DIR + "ClassWithStaticInit.class")
        val outFile = File(CLASS_FILE_TEST_OUT_DIR + "ClassWithStaticInit.out")
        val actOutFile = File(CLASS_FILE_TEST_ACT_OUT_DIR + "ClassWithStaticInit.out.act")
        check(inFile, outFile, actOutFile)
    }

    public fun testInterface() {
        val inFile = File(CLASS_FILE_TEST_RES_DIR + "Interface.class")
        val outFile = File(CLASS_FILE_TEST_OUT_DIR + "Interface.out")
        val actOutFile = File(CLASS_FILE_TEST_ACT_OUT_DIR + "Interface.out.act")
        check(inFile, outFile, actOutFile)
    }
}