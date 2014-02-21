package ru.ifmo.rain.mekhanikov.ant2kotlin.dsl

import java.util.ArrayList
import java.util.HashMap

abstract class DSLElement(val elementName: String) {
    val children: ArrayList<DSLElement> = ArrayList<DSLElement>()
    val attributes = HashMap<String, Any?>()

    protected fun initElement<T: DSLElement>(element: T, init: T.() -> Unit): T {
        element.init()
        children.add(element)
        return element
    }

    abstract fun antObject(parent: Any?) : Any
}