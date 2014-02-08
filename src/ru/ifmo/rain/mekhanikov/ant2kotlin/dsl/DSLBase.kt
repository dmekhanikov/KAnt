package ru.ifmo.rain.mekhanikov.ant2kotlin.dsl

import java.util.ArrayList
import java.util.HashMap

abstract class DSLElement(val elementName: String) {
    val children: ArrayList<DSLElement> = ArrayList<DSLElement>()

    val stringAttributes = HashMap<String, String?>()
    val booleanAttributes = HashMap<String, Boolean?>()
    val charAttributes = HashMap<String, Char?>()
    val byteAttributes = HashMap<String, Byte?>()
    val shortAttributes = HashMap<String, Short?>()
    val intAttributes = HashMap<String, Int?>()
    val floatAttributes = HashMap<String, Float?>()
    val longAttributes = HashMap<String, Long?>()
    val doubleAttributes = HashMap<String, Double?>()

    protected fun initElement<T: DSLElement>(element: T, init: T.() -> Unit): T {
        element.init()
        children.add(element)
        return element
    }

    abstract fun antObject(parent: Any?) : Any
}

/*It would be great to replace all this with some kind of generic*/
public class DSLStringAttributeAdapter() {
    fun get(thisRef: DSLElement, prop: PropertyMetadata) : String? {
        return thisRef.stringAttributes[prop.name]
    }

    fun set(thisRef: DSLElement, prop: PropertyMetadata, v: String?) {
        thisRef.stringAttributes[prop.name] = v
    }
}

public class DSLBooleanAttributeAdapter() {
    fun get(thisRef: DSLElement, prop: PropertyMetadata) : Boolean? {
        return thisRef.booleanAttributes[prop.name]
    }

    fun set(thisRef: DSLElement, prop: PropertyMetadata, v: Boolean?) {
        thisRef.booleanAttributes[prop.name] = v
    }
}

public class DSLCharAttributeAdapter() {
    fun get(thisRef: DSLElement, prop: PropertyMetadata) : Char? {
        return thisRef.charAttributes[prop.name]
    }

    fun set(thisRef: DSLElement, prop: PropertyMetadata, v: Char?) {
        thisRef.charAttributes[prop.name] = v
    }
}

public class DSLByteAttributeAdapter() {
    fun get(thisRef: DSLElement, prop: PropertyMetadata) : Byte? {
        return thisRef.byteAttributes[prop.name]
    }

    fun set(thisRef: DSLElement, prop: PropertyMetadata, v: Byte?) {
        thisRef.byteAttributes[prop.name] = v
    }
}

public class DSLShortAttributeAdapter() {
    fun get(thisRef: DSLElement, prop: PropertyMetadata) : Short? {
        return thisRef.shortAttributes[prop.name]
    }

    fun set(thisRef: DSLElement, prop: PropertyMetadata, v: Short?) {
        thisRef.shortAttributes[prop.name] = v
    }
}

public class DSLIntAttributeAdapter() {
    fun get(thisRef: DSLElement, prop: PropertyMetadata) : Int? {
        return thisRef.intAttributes[prop.name]
    }

    fun set(thisRef: DSLElement, prop: PropertyMetadata, v: Int?) {
        thisRef.intAttributes[prop.name] = v
    }
}

public class DSLFloatAttributeAdapter() {
    fun get(thisRef: DSLElement, prop: PropertyMetadata) : Float? {
        return thisRef.floatAttributes[prop.name]
    }

    fun set(thisRef: DSLElement, prop: PropertyMetadata, v: Float?) {
        thisRef.floatAttributes[prop.name] = v
    }
}

public class DSLLongAttributeAdapter() {
    fun get(thisRef: DSLElement, prop: PropertyMetadata) : Long? {
        return thisRef.longAttributes[prop.name]
    }

    fun set(thisRef: DSLElement, prop: PropertyMetadata, v: Long?) {
        thisRef.longAttributes[prop.name] = v
    }
}

public class DSLDoubleAttributeAdapter() {
    fun get(thisRef: DSLElement, prop: PropertyMetadata) : Double? {
        return thisRef.doubleAttributes[prop.name]
    }

    fun set(thisRef: DSLElement, prop: PropertyMetadata, v: Double?) {
        thisRef.doubleAttributes[prop.name] = v
    }
}