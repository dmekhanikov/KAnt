package jetbrains.kant.translator;

import jetbrains.kant.generator.DSLClass;
import static jetbrains.kant.generator.GeneratorPackage.getSTRUCTURE_FILE_NAME;
import static jetbrains.kant.KantPackage.createClassLoader;
import org.kohsuke.args4j.*;
import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class Translator {
    public static final String TAB = "    ";
    private StringBuilder result;
    private HashMap<String, DSLClass> structure = new HashMap<>();

    @Option(name = "-cp", usage = "classpath with DSL library")
    private String classpath = "";

    @Argument(index = 0)
    private File inputFile;

    @Argument(index = 1)
    private File outputFile;

    public static void main(String ... args) {
        Translator translator = new Translator();
        CmdLineParser parser = new CmdLineParser(translator);
        try {
            parser.parseArgument(args);
            if (translator.outputFile == null) {
                throw new CmdLineException("Too few arguments");
            }
        } catch(CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java Translator [options...] <input file> <output file>");
            parser.printUsage(System.err);
            System.err.println();
            return;
        }
        translator.readStructure();
        try (Writer writer = new FileWriter(translator.outputFile)) {
            InputStream inputStream = new FileInputStream(translator.inputFile);
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

    private void readStructure() {
        String[] classpathArray = classpath.split(File.pathSeparator);
        for (String jar : classpathArray) {
            ClassLoader classLoader = createClassLoader(new String[]{jar});
            InputStream inputStream = classLoader.getResourceAsStream(getSTRUCTURE_FILE_NAME());
            if (inputStream != null) {
                try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
                    HashMap<String, DSLClass> readStructure = (HashMap<String, DSLClass>) objectInputStream.readObject();
                    for (String key : readStructure.keySet()) {
                        structure.put(key, readStructure.get(key));
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class AntXMLHandler extends DefaultHandler {
        List<Wrapper> stack;
        StringBuilder macrodefs;
        PropertyManager propertyManager;

        @Override
        public void startDocument() throws SAXException {
            stack = new ArrayList<>();
            macrodefs = new StringBuilder();
            propertyManager = new PropertyManager();
            result = new StringBuilder();
            result.append("fun main(args: Array<String>) {\n");
        }

        @Override
        public void endDocument () throws SAXException {
            result.append("}\n");
            result.insert(0, "import jetbrains.kant.dsl.*\n\n" + propertyManager + "\n" + macrodefs);
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
                case "property": {
                    Property property = new Property(attrs);
                    propertyManager.writeAccess(property);
                    String propName = attrs.getValue("name");
                    if (propName == null || !propertyManager.isDeclaring()) {
                        processChild(property, parent);
                    }
                    break;
                }
                case "macrodef": {
                    Macrodef macrodef = new Macrodef(attrs);
                    stack.add(macrodef);
                    break;
                }
                case "if": {
                    propertyManager.finishDeclaring();
                    IfStatement ifStatement = new IfStatement();
                    processChild(ifStatement, parent);
                    break;
                }
                case "condition": {
                    propertyManager.finishDeclaring();
                    ConditionTask condition = new ConditionTask(attrs);
                    processChild(condition, parent);
                    break;
                }
                case "sequential": {
                    propertyManager.finishDeclaring();
                    break;
                }
                case "project": {
                    Wrapper project = new Project(attrs);
                    processChild(project, parent);
                    break;
                }
                case "target": {
                    propertyManager.finishDeclaring();
                    Target target = new Target(attrs);
                    processChild(target, parent);
                    break;
                }
                case "attribute": {
                    if (parent != null && parent instanceof Macrodef) {
                        parent.addAttribute(attrs.getValue("name"), attrs.getValue("default"));
                        break;
                    }
                    propertyManager.finishDeclaring();
                }
                default: {
                    propertyManager.finishDeclaring();
                    Wrapper wrapper = new Wrapper(qName, attrs);
                    processChild(wrapper, parent);
                }
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
                    macrodefs.append(wrapper.toString(propertyManager)).append("\n\n");
                } else if (stack.size() == 1) {
                    result.append(stack.get(0).toString(propertyManager));
                    result.append("\n");
                }
                stack.remove(stack.size() - 1);
            }
        }
    }
}
