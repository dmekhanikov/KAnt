import junit.framework.TestCase
import java.io.File
import java.io.BufferedReader
import java.io.FileReader

open class Ant2KotlinTestCase : TestCase() {
    val TEST_ROOT = "test/"
    val TEST_DATA_ROOT = TEST_ROOT + "testData/"
    val TEST_OUT_ROOT = "out/test/Ant2Kotlin/"
    val TEST_DATA_OUT_ROOT = TEST_OUT_ROOT + "testData/"

    protected fun filesDiffer(file1: File, file2: File): Boolean {
        val br1 = BufferedReader(FileReader(file1))
        val br2 = BufferedReader(FileReader(file2))
        var line1: String? = br1.readLine()
        var line2: String? = br2.readLine()
        while (line1 != null && line2 != null) {
            if (!line1.equals(line2)) {
                return false
            }
            line1 = br1.readLine()
            line2 = br2.readLine()
        }
        return line1 == null && line2 == null
    }
}