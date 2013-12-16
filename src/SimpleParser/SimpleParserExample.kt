package SimpleGenerator

import java.io.FileInputStream
import java.io.File

fun main(args: Array<String>) {
    val taskParser = TaskParser(FileInputStream(File(args[0])))
    System.out.println("String fields:")
    for (field in taskParser.stringFields()) {
        System.out.println("\t" + field)
    }
    System.out.println("\nBoolean fields:")
    for (field in taskParser.booleanFields()) {
        System.out.println("\t" + field)
    }
}
