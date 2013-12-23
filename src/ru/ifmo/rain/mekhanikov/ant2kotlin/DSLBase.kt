package ru.ifmo.rain.mekhanikov.ant2kotlin

import java.util.ArrayList
import java.util.HashMap

abstract class DSLElement(val elementName: String) {
    val children: ArrayList<DSLElement> = ArrayList<DSLElement>()
    val attributes = HashMap<String, String?>()

    protected fun initElement<T: DSLElement>(element: T, init: T.() -> Unit): T {
        element.init()
        children.add(element)
        return element
    }

    fun render(builder: StringBuilder, indent: String) {
        builder.append("$indent<$elementName")
        if (!attributes.isEmpty()) {
            val attrIndentBuilder = StringBuilder(indent)
            for (i : Int in 1..(elementName.length + 2)) {
                attrIndentBuilder.append(" ")
            }
            val attrIndent = attrIndentBuilder.toString()
            val renderedAttributes = renderAttributes(attrIndent)
            builder.append(renderedAttributes.substring(attrIndent.length() - 1, renderedAttributes.length() - 1))
        }
        if (!children.isEmpty()) {
            builder.append(">\n")
            for (c in children) {
                c.render(builder, indent + "  ")
            }
            builder.append("$indent</$elementName>\n")
        } else {
            builder.append("/>\n")
        }
    }

    private fun renderAttributes(indent : String): String {
        val builder = StringBuilder()
        for (a in attributes.keySet()) {
            builder.append("$indent$a=\"${attributes[a]}\"\n")
        }
        return builder.toString()
    }

    fun toString(): String {
        val builder = StringBuilder()
        render(builder, "")
        return builder.toString()
    }

    abstract fun antObject(parent: Any?) : Any
}

public class DSLAttributeAdapter() {
    fun get(thisRef: DSLElement, prop: PropertyMetadata) : String? {
        return thisRef.attributes[prop.name]
    }

    fun set(thisRef: DSLElement, prop: PropertyMetadata, v: String?) {
        thisRef.attributes[prop.name] = v
    }
}