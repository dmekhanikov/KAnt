package jetbrains.kant.test

const val DSL_TEST_DATA_DIR = TEST_DATA_DIR + "dsl/"

class DSLTest : KAntTestCase() {
    class DefaultTarget : KAntTestCase() {
        fun testInheritance() {
            runBoxTest(DSL_TEST_DATA_DIR + "DefaultTarget/Inheritance.kt")
        }

        fun testSimple() {
            runBoxTest(DSL_TEST_DATA_DIR + "DefaultTarget/Simple.kt")
        }

        fun testTwoDefaults() {
            runBoxTest(DSL_TEST_DATA_DIR + "DefaultTarget/TwoDefaults.kt")
        }
    }

    class Dependencies : KAntTestCase() {
        fun testChain() {
            runBoxTest(DSL_TEST_DATA_DIR + "Dependencies/Chain.kt")
        }

        fun testCircular() {
            runBoxTest(DSL_TEST_DATA_DIR + "Dependencies/Circular.kt")
        }

        fun testMultipleOccurrences() {
            runBoxTest(DSL_TEST_DATA_DIR + "Dependencies/MultipleOccurrences.kt")
        }

        fun testStar() {
            runBoxTest(DSL_TEST_DATA_DIR + "Dependencies/Star.kt")
        }
    }

    class GenericTask : KAntTestCase() {
        fun testCutDirsMapper() {
            runBoxTest(DSL_TEST_DATA_DIR + "GenericTask/CutDirsMapper.kt")
        }

        fun testMath() {
            runBoxTest(DSL_TEST_DATA_DIR + "GenericTask/Math.kt")
        }

        fun testReplace() {
            runBoxTest(DSL_TEST_DATA_DIR + "GenericTask/Replace.kt")
        }

        fun testSwitch() {
            runBoxTest(DSL_TEST_DATA_DIR + "GenericTask/Switch.kt")
        }
    }

    class NestedElements : KAntTestCase() {
        fun testCutDirsMapper() {
            runBoxTest(DSL_TEST_DATA_DIR + "NestedElements/CutDirsMapper.kt")
        }

        fun testMath() {
            runBoxTest(DSL_TEST_DATA_DIR + "NestedElements/Math.kt")
        }

        fun testReplace() {
            runBoxTest(DSL_TEST_DATA_DIR + "NestedElements/Replace.kt")
        }

        fun testSwitch() {
            runBoxTest(DSL_TEST_DATA_DIR + "NestedElements/Switch.kt")
        }
    }

    class Properties : KAntTestCase() {
        fun testDefaultValues() {
            runBoxTest(DSL_TEST_DATA_DIR + "Properties/DefaultValues.kt")
        }

        fun testGetByName() {
            runBoxTest(DSL_TEST_DATA_DIR + "Properties/GetByName.kt")
        }

        fun testGetFromFile() {
            runBoxTest(DSL_TEST_DATA_DIR + "Properties/GetFromFile.kt")
        }

        fun testInitWithAnotherProp() {
            runBoxTest(DSL_TEST_DATA_DIR + "Properties/InitWithBasedir.kt")
        }

        fun testInvalidFormat() {
            runBoxTest(DSL_TEST_DATA_DIR + "Properties/InvalidFormat.kt")
        }

        fun testMultipleModifications() {
            runBoxTest(DSL_TEST_DATA_DIR + "Properties/MultipleModifications.kt")
        }

        fun testNotInitialized() {
            runBoxTest(DSL_TEST_DATA_DIR + "Properties/NotInitialized.kt")
        }

        fun testSystemProps() {
            runBoxTest(DSL_TEST_DATA_DIR + "Properties/SystemProps.kt")
        }

        fun testTypedProps() {
            runBoxTest(DSL_TEST_DATA_DIR + "Properties/TypedProps.kt")
        }
    }

    class Tasks : KAntTestCase() {
        fun testConditions() {
            runBoxTest(DSL_TEST_DATA_DIR + "Tasks/Conditions.kt")
        }

        fun testExternalLibraries() {
            runBoxTest(DSL_TEST_DATA_DIR + "Tasks/ExternalLibraries.kt")
        }

        fun testTaskdefsInsideTasks() {
            runBoxTest(DSL_TEST_DATA_DIR + "Tasks/TaskdefsInsideTasks.kt")
        }

        fun testTasks() {
            runBoxTest(DSL_TEST_DATA_DIR + "Tasks/Tasks.kt")
        }
    }

    fun testConsistency() {
        runBoxTest(DSL_TEST_DATA_DIR + "Consistency.kt")
    }

    fun testImports() {
        runBoxTest(DSL_TEST_DATA_DIR + "Imports.kt")
    }

    fun testRunner() {
        runBoxTest(DSL_TEST_DATA_DIR + "Runner.kt")
    }
}
