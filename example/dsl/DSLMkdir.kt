import ru.ifmo.rain.mekhanikov.ant2kotlin.dsl.DSLElement
import ru.ifmo.rain.mekhanikov.ant2kotlin.dsl.DSLTarget
import kotlin.properties.Delegates
import java.io.File

class DSLMkdir : DSLElement("mkdir") {
    var dir : File by Delegates.mapVar(attributes)
}

fun DSLTarget.mkdir(init: DSLMkdir.() -> Unit): DSLMkdir =
        initElement(DSLMkdir(), init)