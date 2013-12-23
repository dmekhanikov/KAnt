package ru.ifmo.rain.mekhanikov.ant2kotlin.dslExample

fun main(args: Array<String>) =
    javac {
        srcdir = "/home/user/example/"
        destdir = "/home/user/example/build"
        debug = "true"
    }
