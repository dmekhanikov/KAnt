import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Translator {
    private StringBuilder result;

    public static void main(String ... args) {
        if (args.length != 2) {
            System.out.println("Usage:\n\tjava Translator <input file> <output file>");
            System.exit(1);
        }
        try {
            InputStream inputStream = new FileInputStream(new File(args[0]));
            Writer writer = new FileWriter(new File(args[1]));
            Translator translator = new Translator();
            translator.translate(inputStream, writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void translate(InputStream inputStream, Writer writer) throws IOException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            AntXMLHandler handler = new AntXMLHandler();
            parser.parse(inputStream, handler);
            writer.write(result.toString());
        } catch (SAXException | ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private class AntXMLHandler extends DefaultHandler {
        final String TAB = "    ";

        List<String> stack;
        StringBuilder indent;
        StringBuilder properties;
        StringBuilder macrodefs;
        StringBuilder macrodefIndent;
        boolean closingBracesNeeded;
        boolean bareFunction;
        Macrodef macrodef;
        int macrodefPlace;

        private void swapIndents() {
            StringBuilder temp = macrodefIndent;
            macrodefIndent = indent;
            indent = temp;
        }

        private void renderProperty(Attributes attrs) throws SAXException {
            if (attrs.getValue("file") != null) {
                return;
            }
            String name = attrs.getValue("name");
            String value = attrs.getValue("value");
            if (name == null || value == null) {
                throw new SAXException("Not enough attributes for property");
            }
            if (attrs.getLength() != 2) {
                throw new SAXException("Illegal attributes for property");
            }
            properties.append("val ").append(StringProcessor.toCamelCase(name)).append(": ");
            if (value.equals("true") || value.equals("false") || value.equals("yes") || value.equals("no")) {
                properties.append("Boolean by BooleanProperty(").append(StringProcessor.prepareValue(value));
            } else {
                properties.append("String by StringProperty(").append(StringProcessor.prepareValue(value));
            }
            properties.append(", \"").append(name).append("\")\n");
        }

        private void renderElement(String name, Attributes attributes) {
            result.append(indent).append(StringProcessor.toCamelCase(name));
            if (attributes.getLength() != 0) {
                result.append("(");
                for (int i = 0; i < attributes.getLength(); i++) {
                    String attrName = StringProcessor.toCamelCase(attributes.getQName(i));
                    String attrVal = attributes.getValue(i);
                    if (i != 0) {
                        result.append(", ");
                    }
                    result.append(attrName).append(" = ").append(StringProcessor.prepareValue(attrVal));
                }
                result.append(")");
            } else {
                bareFunction = true;
            }
            closingBracesNeeded = false;
        }

        @Override
        public void startDocument () throws SAXException {
            stack = new ArrayList<>();
            indent = new StringBuilder(TAB);
            properties = new StringBuilder();
            macrodefs = new StringBuilder();
            macrodefIndent = new StringBuilder();
            result = new StringBuilder();
            result.append("fun main(args: Array<String>)");
        }

        @Override
        public void endDocument ()throws SAXException {
            if (!stack.isEmpty()) {
                throw new SAXException("Unclosed tags");
            }
            result.append("}");
            result.insert(0, "import ru.ifmo.rain.mekhanikov.antdsl.*\n\n" + properties + "\n" + macrodefs + "\n");
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
            if (!closingBracesNeeded) {
                result.append(" {\n");
                closingBracesNeeded = true;
            }
            bareFunction = false;
            switch (qName) {
                case "property":
                    renderProperty(attrs);
                    break;
                case "macrodef":
                    macrodef = new Macrodef(attrs);
                    macrodefPlace = result.length();
                    result.append(" {\n");
                    swapIndents();
                    indent.append(TAB);
                    break;
                case "attribute":
                    if (macrodef != null) {
                        macrodef.addAttribute(attrs.getValue("name"), attrs.getValue("default"));
                        break;
                    } else {
                        throw new SAXException("Unexpected attribute element");
                    }
                case "sequential":
                    break;
                default:
                    renderElement(qName, attrs);
                    stack.add(qName);
                    indent.append(TAB);
            }
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            if (!stack.isEmpty() && stack.get(stack.size() - 1).equals(qName)) {
                stack.remove(stack.size() - 1);
                indent.delete(0, TAB.length());
                if (bareFunction) {
                    result.append("()");
                }
                if (closingBracesNeeded) {
                    result.append(indent).append("}\n");
                } else {
                    result.append("\n");
                }
                closingBracesNeeded = true;
            } else if (macrodef != null && qName.equals("macrodef")) {
                if (macrodefs.length() != 0) {
                    macrodefs.append("\n");
                }
                macrodefs.append(macrodef.toString());
                macrodefs.append(result.substring(macrodefPlace)).append("}\n");
                result.delete(macrodefPlace, result.length());
                indent.delete(0, TAB.length());
                swapIndents();
                macrodef = null;
            }
        }
    }
}
