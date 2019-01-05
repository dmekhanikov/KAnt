package jetbrains.kant.test

import jetbrains.kant.translator.Translator
import junit.framework.Assert

class TranslatorTest : KAntTestCase() {
    private fun runTranslatorTest(testName: String) {
        preparePlayground()
        val inputFile = resource("translator/$testName.xml")
        val outputFile = file("$testName.kt")
        Translator.main("-cp", DSL_BIN_DIR, inputFile, outputFile)
        val expected = readFile(resource("translator/$testName.kt"))
        val actual = readFile(outputFile)
        Assert.assertEquals(expected, actual)
    }

    fun testKotlin() {
        runTranslatorTest("kotlin")
    }

    fun testAnt() {
        runTranslatorTest("ant")
    }

    fun testIvy() {
        runTranslatorTest("ivy")
    }
}
