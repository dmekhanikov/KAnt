package jetbrains.kant.test

import junit.framework.TestCase
import java.io.File
import java.io.BufferedReader
import java.io.FileReader
import kotlin.test.assertEquals

abstract class KAntTestCase : TestCase() {
    val TEST_DATA_ROOT = "testData/"
    val TEST_RES_ROOT = TEST_DATA_ROOT + "res/"
    val TEST_OUT_ROOT = "out/test/KAnt/"
    val TEST_DATA_OUT_ROOT = TEST_OUT_ROOT + "testData/"

    private fun readFile(file: File): String {
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

    protected fun assertNotEmpty(file: File) {
        val br = BufferedReader(FileReader(file))
        var line = br.readLine()
        while (line != null && line!!.trim().isEmpty()) {
            line = br.readLine()
        }
        assert(line != null, "File is empty.")
    }
}
