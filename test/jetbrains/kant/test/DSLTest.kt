package jetbrains.kant.test

class GeneratorTest : KAntTestCase() {
    val DSL_TEST_DATA_DIR = TEST_DATA_DIR + "dsl/";

    public fun testTasks() {
        runBoxTest(DSL_TEST_DATA_DIR + "Tasks.kt")
    }

    public fun testConditions() {
        runBoxTest(DSL_TEST_DATA_DIR + "Conditions.kt")
    }

    public fun testDefault() {
        runBoxTest(DSL_TEST_DATA_DIR + "Default.kt")
    }

    public fun testDepends() {
        runBoxTest(DSL_TEST_DATA_DIR + "Depends.kt")
    }

    public fun testProperties() {
        runBoxTest(DSL_TEST_DATA_DIR + "Properties.kt")
    }

    public fun testImports() {
        runBoxTest(DSL_TEST_DATA_DIR + "Imports.kt")
    }

    public fun testExternalLibraries() {
        runBoxTest(DSL_TEST_DATA_DIR + "ExternalLibraries.kt")
    }

    public fun testConsistency() {
        runBoxTest(DSL_TEST_DATA_DIR + "Consistency.kt")
    }

    public fun testRunner() {
        runBoxTest(DSL_TEST_DATA_DIR + "Runner.kt")
    }
}
