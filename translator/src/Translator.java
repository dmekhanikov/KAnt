import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;

public class Translator {
    public static final String TAB = "    ";
    private StringBuilder result;

    public static void main(String ... args) {
        if (args.length != 2) {
            System.out.println("Usage:\n\tjava Translator <input file> <output file>");
            System.exit(1);
        }
        try (Writer writer = new FileWriter(new File(args[1]))) {
            InputStream inputStream = new FileInputStream(new File(args[0]));
            Translator translator = new Translator();
            translator.translate(inputStream, writer);
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
        List<Wrapper> stack;
        StringBuilder properties;
        StringBuilder macrodefs;

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
            String ccName = StringProcessor.toCamelCase(name);
            properties.append("val ").append(ccName).append(": ");
            if (value.equals("true") || value.equals("false") || value.equals("yes") || value.equals("no")) {
                properties.append("Boolean by BooleanProperty");
            } else {
                properties.append("String by StringProperty");
            }
            if (!name.equals(ccName)) {
                properties.append("(\"").append(name).append("\")");
            }
            properties.append(" { ").append(StringProcessor.prepareValue(value)).append(" }\n");
        }

        @Override
        public void startDocument() throws SAXException {
            stack = new ArrayList<>();
            properties = new StringBuilder();
            macrodefs = new StringBuilder();
            result = new StringBuilder();
            result.append("fun main(args: Array<String>) {\n");
        }

        @Override
        public void endDocument () throws SAXException {
            result.append("}\n");
            result.insert(0, "import ru.ifmo.rain.mekhanikov.antdsl.*\n\n" + properties + "\n" + macrodefs);
        }

        private void processChild(Wrapper child, Wrapper parent) throws SAXException {
            if (parent != null) {
                child = parent.addChild(child);
            } else {
                child.setIndent(TAB);
            }
            stack.add(child);
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
            Wrapper parent = null;
            if (!stack.isEmpty()) {
                parent = stack.get(stack.size() - 1);
            }
            switch (qName) {
                case "property":
                    renderProperty(attrs);
                    break;
                case "macrodef":
                    Macrodef macrodef = new Macrodef(attrs);
                    stack.add(macrodef);
                    break;
                case "if":
                    IfStatement ifStatement = new IfStatement();
                    processChild(ifStatement, parent);
                    break;
                case "sequential":
                    break;
                case "project":
                    Wrapper project = new Project(attrs);
                    processChild(project, parent);
                    break;
                case "target":
                    Target target = new Target(attrs);
                    processChild(target, parent);
                    break;
                case "attribute":
                    if (parent != null && parent instanceof Macrodef) {
                        parent.addAttribute(attrs.getValue("name"), attrs.getValue("default"));
                        break;
                    }
                default:
                    Wrapper wrapper = new Wrapper(qName, attrs);
                    processChild(wrapper, parent);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String value = new String(ch, start, length);
            if (!value.trim().isEmpty()) {
                Wrapper parent = stack.get(stack.size() - 1);
                Text text = new Text();
                text.setText(value);
                parent.addChild(text);
            }
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            if (!stack.isEmpty() && stack.get(stack.size() - 1).name.equals(qName)) {
                Wrapper wrapper = stack.get(stack.size() - 1);
                if (wrapper instanceof Macrodef) {
                    macrodefs.append(wrapper.toString()).append("\n\n");
                } else if (stack.size() == 1) {
                    result.append(stack.get(0).toString());
                    result.append("\n");
                }
                stack.remove(stack.size() - 1);
            }
        }
    }
}
