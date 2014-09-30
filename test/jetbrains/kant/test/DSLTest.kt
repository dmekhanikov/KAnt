package jetbrains.kant.test

import junit.framework.TestCase

val DSL_TEST_DATA_DIR = TEST_DATA_DIR + "dsl/";

class DSLTest : KAntTestCase() {
    public class DefaultTarget : KAntTestCase() {
        public fun testInheritance() {
            runBoxTest(DSL_TEST_DATA_DIR + "DefaultTarget/Inheritance.kt")
        }

        public fun testSimple() {
            runBoxTest(DSL_TEST_DATA_DIR + "DefaultTarget/Simple.kt")
        }

        public fun testTwoDefaults() {
            runBoxTest(DSL_TEST_DATA_DIR + "DefaultTarget/TwoDefaults.kt")
        }
    }

    public class Dependencies : KAntTestCase() {
        public fun testChain() {
            runBoxTest(DSL_TEST_DATA_DIR + "Dependencies/Chain.kt")
        }

        public fun testCircular() {
            runBoxTest(DSL_TEST_DATA_DIR + "Dependencies/Circular.kt")
        }

        public fun testMultipleOccurrences() {
            runBoxTest(DSL_TEST_DATA_DIR + "Dependencies/MultipleOccurrences.kt")
        }

        public fun testStar() {
            runBoxTest(DSL_TEST_DATA_DIR + "Dependencies/Star.kt")
        }
    }

    public class NestedElements : KAntTestCase() {
        public fun testCutDirsMapper() {
            runBoxTest(DSL_TEST_DATA_DIR + "NestedElements/CutDirsMapper.kt")
        }

        public fun testMath() {
            runBoxTest(DSL_TEST_DATA_DIR + "NestedElements/Math.kt")
        }

        public fun testReplace() {
            runBoxTest(DSL_TEST_DATA_DIR + "NestedElements/Replace.kt")
        }

        public fun testSwitch() {
            runBoxTest(DSL_TEST_DATA_DIR + "NestedElements/Switch.kt")
        }
    }

    public class Properties : KAntTestCase() {
        public fun testDefaultValues() {
            runBoxTest(DSL_TEST_DATA_DIR + "Properties/DefaultValues.kt")
        }

        public fun testGetByName() {
            runBoxTest(DSL_TEST_DATA_DIR + "Properties/GetByName.kt")
        }

        public fun testGetFromFile() {
            runBoxTest(DSL_TEST_DATA_DIR + "Properties/GetFromFile.kt")
        }

        public fun testInvalidFormat() {
            runBoxTest(DSL_TEST_DATA_DIR + "Properties/InvalidFormat.kt")
        }

        public fun testMultipleModifications() {
            runBoxTest(DSL_TEST_DATA_DIR + "Properties/MultipleModifications.kt")
        }

        public fun testNotInitialized() {
            runBoxTest(DSL_TEST_DATA_DIR + "Properties/NotInitialized.kt")
        }

        public fun testSystemProps() {
            runBoxTest(DSL_TEST_DATA_DIR + "Properties/SystemProps.kt")
        }

        public fun testTypedProps() {
            runBoxTest(DSL_TEST_DATA_DIR + "Properties/TypedProps.kt")
        }
    }

    public class Tasks : KAntTestCase() {
        public fun testConditions() {
            runBoxTest(DSL_TEST_DATA_DIR + "Tasks/Conditions.kt")
        }

        public fun testExternalLibraries() {
            runBoxTest(DSL_TEST_DATA_DIR + "Tasks/ExternalLibraries.kt")
        }

        public fun testTaskdefsInsideTasks() {
            runBoxTest(DSL_TEST_DATA_DIR + "Tasks/TaskdefsInsideTasks.kt")
        }

        public fun testTasks() {
            runBoxTest(DSL_TEST_DATA_DIR + "Tasks/Tasks.kt")
        }
    }

    public fun testConsistency() {
        runBoxTest(DSL_TEST_DATA_DIR + "Consistency.kt")
    }

    public fun testImports() {
        runBoxTest(DSL_TEST_DATA_DIR + "Imports.kt")
    }

    public fun testRunner() {
        runBoxTest(DSL_TEST_DATA_DIR + "Runner.kt")
    }
}
