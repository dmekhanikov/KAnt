package testData.DSLGenerator.conditions

import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.*
import jetbrains.kant.dsl.taskdefs.condition.*

val booleanProperty by BooleanProperty()
val string by StringProperty()
val pattern by StringProperty()
val arg1 by StringProperty()
val arg2 by StringProperty()
val file by StringProperty()

fun main(args: Array<String>) {
    object : DSLProject(args) {
        {
            val message = StringBuilder()
            message.append(istrue(booleanProperty))!!.append("\n")
            message.append(matches(string = string, pattern = pattern))!!.append("\n")
            message.append(equals(arg1 = arg1, arg2 = arg2))!!.append("\n")
            echo(message = message.toString(), file = file)
        }
    }
}
