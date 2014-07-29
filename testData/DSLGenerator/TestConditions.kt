package testData.DSLGenerator.conditions

import ru.ifmo.rain.mekhanikov.antdsl.*
import ru.ifmo.rain.mekhanikov.antdsl.taskdefs.*
import ru.ifmo.rain.mekhanikov.antdsl.taskdefs.condition.*

val booleanProperty by BooleanProperty()
val string by StringProperty()
val pattern by StringProperty()
val arg1 by StringProperty()
val arg2 by StringProperty()
val file by StringProperty()

fun main(args: Array<String>) {
    project(args) {
        val message = StringBuilder()
        message.append(istrue(booleanProperty))!!.append("\n")
        message.append(matches(string = string, pattern = pattern))!!.append("\n")
        message.append(equals(arg1 = arg1, arg2 = arg2))!!.append("\n")
        echo(message = message.toString(), file = file)
    }
}
