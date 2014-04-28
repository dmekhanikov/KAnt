package ru.ifmo.rain.mekhanikov.antdsl

import java.util.regex.Pattern

public fun matches(pattern: String, string: String): Boolean {
    return Pattern.compile(pattern)!!.matcher(string)!!.matches()
}
