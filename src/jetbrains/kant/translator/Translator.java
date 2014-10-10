package jetbrains.kant.translator;

import jetbrains.kant.gtcommon.ImportManager;
import jetbrains.kant.generator.DSLClass;
import static jetbrains.kant.common.CommonPackage.createClassLoader;
import static jetbrains.kant.gtcommon.constants.ConstantsPackage.*;

import jetbrains.kant.generator.DSLFunction;
import jetbrains.kant.translator.codeStructure.Context;
import jetbrains.kant.translator.codeStructure.Reference;
import jetbrains.kant.translator.wrappers.*;
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

    @Option(name = "-cp", metaVar = "<path>", usage = "Classpath with DSL library")
    private String classpath = "";

    @Argument(index = 0, metaVar = "input file", usage = "Input file with an Ant script", required = true)
    private File inputFile;

    @Argument(index = 1, metaVar = "output file", required = true)
    private File outputFile;

    public static void main(String ... args) {
        Translator translator = new Translator();
        CmdLineParser parser = new CmdLineParser(translator);
        try {
            parser.parseArgument(args);
        } catch(CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("Usage: java jetbrains.kant.translator.TranslatorPackage <options> <input file> <output file>");
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
            ClassLoader classLoader = createClassLoader(jar, null);
            InputStream inputStream = classLoader.getResourceAsStream(getSTRUCTURE_FILE());
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
        StringBuilder macrodefs;
        Context globalContext;
        Context currentContext;

        PropertyManager propertyManager;
        ImportManager importManager;
        Map<String, Reference> references;

        @Override
        public void startDocument() throws SAXException {
            macrodefs = new StringBuilder();
            propertyManager = new PropertyManager();
            importManager = new ImportManager(null);
            references = new HashMap<>();
            globalContext = new Context(null, propertyManager, importManager, references);
            currentContext = globalContext;
            result = new StringBuilder();
        }

        @Override
        public void endDocument() throws SAXException {
            String propertiesDeclaration = propertyManager.toString(currentContext);
            result.insert(0, importManager + "\n" + propertiesDeclaration + "\n" + macrodefs);
        }

        DSLFunction findConstructor(String parentClassName, String name) {
            DSLClass parentDSLClass = structure.get(parentClassName);
            if (parentDSLClass != null) {
                DSLFunction constructor = parentDSLClass.getFunction(name);
                if (constructor != null) {
                    return constructor;
                } else {
                    for (String trait : parentDSLClass.getTraits()) {
                        constructor = findConstructor(trait, name);
                        if (constructor != null) {
                            return constructor;
                        }
                    }
                }
            }
            return null;
        }

        DSLFunction findConstructor(Wrapper parent, String name) {
            if (parent == null) {
                return null;
            }
            return findConstructor(parent.getDSLClassName(), name);
        }

        void processChild(Wrapper child, Wrapper parent) throws SAXException {
            if (parent != null) {
                parent.addChild(child);
            }
        }

        void defaultHandler(String name, Attributes attrs, Wrapper parent, DSLFunction constructor) throws SAXException {
            propertyManager.finishDeclaring();
            Wrapper wrapper;
            if (constructor != null) {
                wrapper = new Wrapper(constructor, attrs, currentContext);
            } else {
                wrapper = new Wrapper(name, attrs, currentContext);
            }
            processChild(wrapper, parent);
        }

        @Override
        public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
            Wrapper parent = currentContext.getWrapper();
            currentContext = new Context(currentContext);
            DSLFunction constructor = findConstructor(parent, qName);
            if (constructor != null && !constructor.getParentName().equals(getDSL_TASK_CONTAINER())) { // it's a nested element
                defaultHandler(qName, attrs, parent, constructor);
                return;
            }
            switch (qName) {
                case "project": {
                    Wrapper project = new Project(attrs, currentContext);
                    processChild(project, parent);
                    break;
                }
                case "target": {
                    propertyManager.finishDeclaring();
                    Target target = new Target(attrs, currentContext);
                    processChild(target, parent);
                    break;
                }
                case "property": {
                    Property property;
                    if (constructor != null) {
                         property = new Property(attrs, constructor, currentContext);
                    } else {
                        property = new Property(attrs, currentContext);
                    }
                    propertyManager.writeAccess(property);
                    String propName = attrs.getValue("name");
                    if (propName == null || !propertyManager.isDeclaring()) {
                        processChild(property, parent);
                    }
                    break;
                }
                case "if": {
                    propertyManager.finishDeclaring();
                    IfStatement ifStatement = new IfStatement(currentContext);
                    processChild(ifStatement, parent);
                    break;
                }
                case "condition": {
                    propertyManager.finishDeclaring();
                    ConditionTask condition = new ConditionTask(attrs, currentContext);
                    processChild(condition, parent);
                    break;
                }
                case "sequential": {
                    propertyManager.finishDeclaring();
                    Sequential sequential = new Sequential(currentContext);
                    processChild(sequential, parent);
                    break;
                }
                case "macrodef": {
                    new Macrodef(attrs, currentContext); // it is written as wrapper for currentNode
                    break;
                }
                case "attribute": {
                    if (parent != null && parent instanceof Macrodef) {
                        String attrName = attrs.getValue("name");
                        String attrVal = attrs.getValue("default");
                        String attrType = StringProcessor.getType(attrVal);
                        DSLAttribute attribute = new DSLAttribute(attrName, attrType, attrVal);
                        parent.addAttribute(attribute);
                        currentContext.getParent().addFunctionArgument(attrName, attrType);
                        break;
                    }
                }
                default: {
                    defaultHandler(qName, attrs, parent, constructor);
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String value = new String(ch, start, length);
            if (!value.trim().isEmpty()) {
                Wrapper wrapper = currentContext.getWrapper();
                wrapper.addText(value);
            }
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            Wrapper wrapper = currentContext.getWrapper();
            Context parentalContext = currentContext.getParent();
            if (wrapper instanceof Macrodef) {
                Macrodef macrodef = (Macrodef) wrapper;
                DSLClass dslTaskContainerClass = structure.get(getDSL_TASK_CONTAINER());
                if (dslTaskContainerClass == null) {
                    dslTaskContainerClass = new DSLClass(getDSL_TASK_CONTAINER(), new ArrayList<String>());
                    structure.put(getDSL_TASK_CONTAINER(), dslTaskContainerClass);
                }
                dslTaskContainerClass.addFunction(macrodef.getMacrodefName(), getDSL_PACKAGE(),
                        macrodef.getAttributes(), getDSL_TASK_CONTAINER(), null);
                currentContext.setOffset(macrodefs.length());
                macrodefs.append(macrodef.toString()).append("\n\n");
                currentContext.setParent(globalContext);
            } else if (currentContext.getParent() == globalContext) {
                currentContext.setOffset(result.length());
                result.append(wrapper.toString());
                result.append("\n");
            }
            currentContext.getParent().addChild(currentContext);
            currentContext = parentalContext;
        }
    }
}
