package jetbrains.kant.test

import junit.framework.TestCase
import java.io.File
import java.io.BufferedReader
import java.io.FileReader
import kotlin.test.assertEquals

abstract class KAntTestCase : TestCase() {
    val TEST_DATA_DIR = "testData/"
    val TEST_RES_DIR = TEST_DATA_DIR + "res/"
    val TEST_BIN_DIR = "out/test/KAnt/"
    val DSL_BIN_DIR = TEST_BIN_DIR + "dsl/"
    val TEST_PLAYGROUND_DIR = TEST_BIN_DIR + "playground/"
    val TEST_PLAYGROUND_BIN_DIR = TEST_PLAYGROUND_DIR + "bin/"
    val TEST_PLAYGROUND_WORK_DIR = TEST_PLAYGROUND_DIR + "work/"

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

    class Property(val name: String, val value: String)

    protected fun setProperties(properties: Array<Property>?) {
        if (properties != null) {
            for (property in properties) {
                System.setProperty(property.name, property.value)
            }
        }
    }

    protected fun clearProperties(properties: Array<Property>?) {
        if (properties != null) {
            for (property in properties) {
                System.clearProperty(property.name)
            }
        }
    }
}
