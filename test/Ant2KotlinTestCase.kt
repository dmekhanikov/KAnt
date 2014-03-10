import junit.framework.TestCase
import java.io.File
import java.io.BufferedReader
import java.io.FileReader
import kotlin.test.assertEquals

open class Ant2KotlinTestCase : TestCase() {
    val TEST_ROOT = "test/"
    val TEST_DATA_ROOT = "testData/"
    val TEST_RES_ROOT = TEST_DATA_ROOT + "res/"
    val TEST_OUT_ROOT = "out/test/Ant2Kotlin/"
    val TEST_DATA_OUT_ROOT = TEST_OUT_ROOT + "testData/"

    protected fun createDirectory(path : String) {
        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    private fun readFile(file : File): String {
        val br = BufferedReader(FileReader(file))
        var line = br.readLine()
        val res = StringBuilder("")
        while (line != null) {
            res.append(line + "\n")
            line = br.readLine()
        }
        return res.toString()
    }

    protected fun assertFilesMatch(expected: File, actual: File) {
        if (!expected.exists()) {
            assert(false, "File with expected data does not exist.")
        }
        if (!actual.exists()) {
            assert(false, "File with actual data does not exist.")
        }
        assertEquals(readFile(expected), readFile(actual))
    }
}