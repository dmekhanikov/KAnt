package jetbrains.kant.test

import jetbrains.kant.gtcommon.constants.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*

const val TEST_DATA_DIR = "testData/"
const val TEST_RES_DIR = TEST_DATA_DIR + "res/"
const val TEST_BIN_DIR = "out/test/KAnt/"
const val GT_BIN_DIR = "out/production/KAnt/"
const val GENERATOR_OUT_DIR = TEST_BIN_DIR + "dsl/"
const val DSL_BIN_DIR = GENERATOR_OUT_DIR + "bin/"
const val TEST_PLAYGROUND_DIR = TEST_BIN_DIR + "playground/"
const val TEST_PLAYGROUND_BIN_DIR = TEST_PLAYGROUND_DIR + "bin/"
const val TEST_PLAYGROUND_WORK_DIR = TEST_PLAYGROUND_DIR + "work/"
val DSL_DEPENDS = arrayOf(ANT_JAR, ANT_LAUNCHER_JAR,
        ANT_CONTRIB_JAR, ARGS4J_JAR, KOTLIN_RUNTIME_JAR)

fun file(fileName: String): String {
    if (fileName.startsWith('/')) {
        return fileName
    } else {
        return TEST_PLAYGROUND_WORK_DIR + fileName
    }
}

fun resource(fileName: String): String {
    if (fileName.startsWith('/')) {
        return fileName
    } else {
        return TEST_DATA_DIR + fileName
    }
}

fun readFile(fileName: String): String {
    val file = File(fileName)
    val br = BufferedReader(FileReader(file))
    var line = br.readLine()
    val res = StringBuilder("")
    while (line != null) {
        res.append(line + "\n")
        line = br.readLine()
    }
    return res.toString()
}

class PropertyManager {
    val properties = HashMap<String, String?>()

    fun setProperty(name: String, value: String) {
        properties[name] = System.getProperty(name)
        System.setProperty(name, value)
    }

    fun restore() {
        for ((name, value) in properties) {
            if (value != null) {
                System.setProperty(name, value)
            } else {
                System.clearProperty(name)
            }
        }
    }
}
