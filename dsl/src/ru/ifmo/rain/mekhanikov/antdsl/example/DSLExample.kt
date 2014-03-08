package ru.ifmo.rain.mekhanikov.antdsl.example

import java.io.File
import ru.ifmo.rain.mekhanikov.antdsl.project
import ru.ifmo.rain.mekhanikov.antdsl.taskdefs.mkdir

fun main(args : Array<String>) =
    project {
        default = "mkdir"
        target("mkdir") {
            mkdir {
                dir = File("temp")
            }
        }
    }
