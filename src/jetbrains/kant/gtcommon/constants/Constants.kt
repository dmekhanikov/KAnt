package jetbrains.kant.gtcommon.constants

const val KANT_PACKAGE: String = "jetbrains.kant"
const val DSL_PACKAGE: String = "$KANT_PACKAGE.dsl"
const val COMMON_PACKAGE: String = "$KANT_PACKAGE.common"
const val DSL_PROPERTIES_PACKAGE: String = DSL_PACKAGE
const val ANT_CLASS_PREFIX: String = "org.apache.tools.ant."

const val DSL_SRC_ROOT: String = "dsl/src/"
val DSL_GENERATED_DIR: String = DSL_SRC_ROOT + DSL_PACKAGE.replace(".", "/") + "/generated/"
const val COMMON_SRC_DIR: String = "common/src"
const val STRUCTURE_FILE: String = "resources/structure.ser"
const val KOTLIN_RUNTIME_JAR: String = "lib/kotlin-runtime.jar"
const val ANT_JAR: String = "lib/ant.jar"
const val ANT_LAUNCHER_JAR: String = "lib/ant-launcher.jar"
const val ANT_CONTRIB_JAR: String = "lib/ant-contrib.jar"
const val ARGS4J_JAR: String = "lib/args4j.jar"

val keywords: Set<String> = setOf(
        "package",
        "as",
        "type",
        "class",
        "this",
        "super",
        "val",
        "var",
        "fun",
        "for",
        "null",
        "true",
        "false",
        "is",
        "in",
        "throw",
        "return",
        "break",
        "continue",
        "object",
        "if",
        "try",
        "else",
        "while",
        "do",
        "when",
        "trait",
        "This"
)

const val DSL_TASK_CONTAINER: String = "$DSL_PACKAGE.DSLTaskContainer"
const val DSL_TASK_CONTAINER_TASK: String = "$DSL_PACKAGE.DSLTaskContainerTask"
const val DSL_PROJECT: String = "$DSL_PACKAGE.DSLProject"
const val DSL_TARGET: String = "$DSL_PACKAGE.DSLTarget"
const val DSL_TASK: String = "$DSL_PACKAGE.DSLTask"
const val DSL_REFERENCE: String = "$DSL_PACKAGE.DSLReference"
const val DSL_PATH: String = "$DSL_PACKAGE.types.DSLPath"
const val DSL_TEXT_CONTAINER: String = "$DSL_PACKAGE.DSLTextContainer"
const val DSL_CONDITION: String = "$DSL_PACKAGE.DSLCondition"
const val DSL_PROJECT_FUNCTION: String = "$DSL_PACKAGE.project"
const val DSL_TARGET_FUNCTION: String = "$DSL_PACKAGE.target"
