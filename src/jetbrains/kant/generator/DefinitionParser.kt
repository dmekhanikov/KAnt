package jetbrains.kant.generator

import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import java.util.ArrayList
import java.util.regex.Pattern
import java.io.InputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.xml.parsers.SAXParserFactory
import javax.xml.parsers.ParserConfigurationException
import jetbrains.kant.common.DefinitionKind
import java.util.HashMap

class DefinitionParser(private val inputStream: InputStream) {
    fun parseProperties(): List<Definition> {
        val result = ArrayList<Definition>()
        val bf = BufferedReader(InputStreamReader(inputStream))
        var line = bf.readLine()
        while (line != null) {
            val definition = parseDefinition(line)
            if (definition != null) {
                result.add(definition)
            }
            line = bf.readLine()
        }
        return result
    }

    private fun parseDefinition(line: String): Definition? {
        val pattern = Pattern.compile("^([\\w.-]*)\\s*=\\s*([\\w.]*)$")
        val matcher = pattern.matcher(line)
        return if (matcher.matches()) {
            val name = matcher.group(1)!!
            val className = matcher.group(2)!!
            Definition(name = name, className = className, kind = DefinitionKind.TASK)
        } else {
            null
        }
    }

    fun parseXML(): List<Definition> {
        try {
            val factory  = SAXParserFactory.newInstance()!!
            val parser = factory.newSAXParser()
            val handler = AntlibHandler()
            parser.parse(inputStream, handler)
            return handler.result
        } catch (ignore: SAXException) {
        } catch (e: ParserConfigurationException) {
            e.printStackTrace()
        }
        return ArrayList()
    }

    private class AntlibHandler: DefaultHandler() {
        var depth = 0
        val result = ArrayList<Definition>()

        override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
            depth++
            if (qName == "antlib" && depth != 1 || qName != "antlib" && depth == 1) {
                throw SAXException("Not an antlib file")
            }
            if (qName == "antlib" || qName != "taskdef" && qName != "typedef" && qName != "componentdef") {
                return
            }
            val name = attributes.getValue("name")
            val classname = attributes.getValue("classname")
            if (name != null && classname != null) {
                val kind = when (qName) {
                    "taskdef" -> DefinitionKind.TASK
                    "typedef" -> DefinitionKind.TYPE
                    "componentdef" -> DefinitionKind.COMPONENT
                    else -> return
                }
                val extraAttributes = HashMap<String, String>()
                for (i in 0 until attributes.getLength()) {
                    val key = attributes.getLocalName(i)!!
                    if (key == "onerror" || key == "adapter" || key == "adaptto" || key == "uri") {
                        val value = attributes.getValue(i)!!
                        extraAttributes[key] = value
                    }
                }
                result.add(
                        Definition(
                                name = name,
                                className = classname,
                                kind = kind,
                                extraAttributes = extraAttributes
                        ))
            }
        }

        override fun endElement(uri: String?, localName: String, qName: String) {
            depth--
        }
    }
}
