package ru.ifmo.rain.mekhanikov.ant2kotlin.dsl

import ru.ifmo.rain.mekhanikov.ant2kotlin.AntClassAttribute

fun DSLElementTemplate(name : String, attributes: String): String {
    return  "import ru.ifmo.rain.mekhanikov.ant2kotlin.dsl.DSLElement\n" +
            "import ru.ifmo.rain.mekhanikov.ant2kotlin.dsl.DSLStringAttributeAdapter\n" +
            "import ru.ifmo.rain.mekhanikov.ant2kotlin.dsl.DSLBooleanAttributeAdapter\n" +
            "import ru.ifmo.rain.mekhanikov.ant2kotlin.dsl.DSLCharAttributeAdapter\n" +
            "import ru.ifmo.rain.mekhanikov.ant2kotlin.dsl.DSLByteAttributeAdapter\n" +
            "import ru.ifmo.rain.mekhanikov.ant2kotlin.dsl.DSLShortAttributeAdapter\n" +
            "import ru.ifmo.rain.mekhanikov.ant2kotlin.dsl.DSLIntAttributeAdapter\n" +
            "import ru.ifmo.rain.mekhanikov.ant2kotlin.dsl.DSLFloatAttributeAdapter\n" +
            "import ru.ifmo.rain.mekhanikov.ant2kotlin.dsl.DSLLongAttributeAdapter\n" +
            "import ru.ifmo.rain.mekhanikov.ant2kotlin.dsl.DSLDoubleAttributeAdapter\n" +
            "\n" +
            "class DSL$name : DSLElement(\"$name\") {\n" +
            attributes +
            "}\n"
}

fun DSLAttributesTemplate(attributes : List<AntClassAttribute>): String {
    val res = StringBuilder("")
    for (a in attributes) {
        res.append("    var ${a.name}: ${a.typeName}? by DSL${a.typeName}AttributeAdapter()\n")
    }
    return res.toString()
}