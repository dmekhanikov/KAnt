package ru.ifmo.rain.mekhanikov.antdsl

var maxRefid = 0

class DSLReference<T : DSLElement>(public val value: T) {
    val refid = (++maxRefid).toString();
    {
        value.attributes["id"] = refid
    }
}

class DSLLoaderRef(val value : String)
