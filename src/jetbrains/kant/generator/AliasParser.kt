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

class Alias(val tag: String, val className: String, val restricted: Boolean)

class AliasParser(val inputStream: InputStream) {
    fun parseProperties(): List<Alias> {
        val result = ArrayList<Alias>()
        val bf = BufferedReader(InputStreamReader(inputStream))
        var line = bf.readLine()
        while (line != null) {
            val alias = parseAlias(line!!)
            if (alias != null) {
                result.add(alias)
            }
            line = bf.readLine()
        }
        return result
    }

    private fun parseAlias(line: String): Alias? {
        val pattern = Pattern.compile("^([\\w.-]*)\\s*=\\s*([\\w.]*)$")
        val matcher = pattern.matcher(line)
        if (matcher.matches()) {
            val tag = matcher.group(1)!!
            val className = matcher.group(2)!!
            return Alias(tag, className, false)
        } else {
            return null
        }
    }

    fun parseXML(): List<Alias> {
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
        return ArrayList<Alias>()
    }

    private class AntlibHandler: DefaultHandler() {
        var depth = 0
        val result = ArrayList<Alias>()

        override fun startElement(uri: String, localName: String, qName: String, attributes: Attributes) {
            depth++
            if (qName == "antlib" && depth != 1 || qName != "antlib" && depth == 1) {
                throw SAXException("Not an antlib file")
            }
            if (qName == "antlib" || qName != "taskdef" && qName != "typedef" && qName != "componentdef") {
                return
            }
            val name = attributes.getValue("name")
            val className = attributes.getValue("classname")
            if (name != null && className != null) {
                val restricted = when (qName) {
                    "taskdef", "typedef" -> false
                    else -> true
                }
                result.add(Alias(name, className, restricted))
            }
        }
        override fun endElement(uri: String?, localName: String, qName: String) {
            depth--
        }
    }
}
