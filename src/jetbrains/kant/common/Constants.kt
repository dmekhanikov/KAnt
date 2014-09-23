package jetbrains.kant.gtcommon.constants

import java.util.HashSet

public val KANT_PACKAGE: String = "jetbrains.kant"
public val DSL_PACKAGE: String = "$KANT_PACKAGE.dsl"
public val DSL_PROPERTIES_PACKAGE: String = DSL_PACKAGE
public val ANT_CLASS_PREFIX: String = "org.apache.tools.ant."

public val DSL_SRC_ROOT: String = "dsl/src/"
public val DSL_GENERATED_DIR: String = DSL_SRC_ROOT + DSL_PACKAGE.replace(".", "/") + "/generated/"
public val COMMON_SRC_DIR: String = "common/src"
public val STRUCTURE_FILE: String = "resources/structure.ser"
public val KOTLIN_RUNTIME_JAR: String = "lib/kotlin-runtime.jar"
public val ANT_JAR: String = "lib/ant.jar"
public val ANT_LAUNCHER_JAR: String = "lib/ant-launcher.jar"
public val ANT_CONTRIB_JAR: String = "lib/ant-contrib.jar"
public val ARGS4J_JAR: String = "lib/args4j.jar"

public val keywords: Set<String> = array(
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
).toHashSet()

public val DSL_TASK_CONTAINER: String = "$DSL_PACKAGE.DSLTaskContainer"
public val DSL_TASK_CONTAINER_TASK: String = "$DSL_PACKAGE.DSLTaskContainerTask"
public val DSL_PROJECT: String = "$DSL_PACKAGE.DSLProject"
public val DSL_TARGET: String = "$DSL_PACKAGE.DSLTarget"
public val DSL_TASK: String = "$DSL_PACKAGE.DSLTask"
public val DSL_REFERENCE: String = "$DSL_PACKAGE.DSLReference"
public val DSL_PATH: String = "$DSL_PACKAGE.types.DSLPath"
public val DSL_TEXT_CONTAINER: String = "$DSL_PACKAGE.DSLTextContainer"
public val DSL_CONDITION: String = "$DSL_PACKAGE.DSLCondition"
public val DSL_PROJECT_FUNCTION: String = "$DSL_PACKAGE.project"
public val DSL_TARGET_FUNCTION: String = "$DSL_PACKAGE.target"
