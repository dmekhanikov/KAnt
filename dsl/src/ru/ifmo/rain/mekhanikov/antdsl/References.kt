package ru.ifmo.rain.mekhanikov.antdsl

var maxRefid = 0

class Reference<T : DSLElement>(public val value: T) {
    val refid = (++maxRefid).toString();
    {
        value.attributes["id"] = refid
    }
}

class LoaderRef(val value : String)
