package jetbrains.kant.translator;

import static jetbrains.kant.KantPackage.toCamelCase;
import static jetbrains.kant.generator.GeneratorPackage.getDSL_TASK_CONTAINER;
import jetbrains.kant.ImportManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class Macrodef extends Wrapper {
    private String macrodefName;

    public Macrodef(Attributes attrs) throws SAXException {
        super((String) null, null);
        macrodefName = attrs.getValue("name");
        if (macrodefName == null) {
            throw new SAXException("Macrodef should have a name");
        }
        if (attrs.getLength() != 1) {
            throw new SAXException("Illegal attributes for macrodef");
        }
        macrodefName = toCamelCase(macrodefName);
    }

    public String toString(PropertyManager propertyManager, ImportManager importManager) {
        String taskContainerShorten = importManager.shorten(getDSL_TASK_CONTAINER());
        return indent + "fun DSLTaskContainer." + macrodefName + renderAttributes(true, propertyManager) + " {\n"
                + renderChildren(propertyManager, importManager) + "\n"
                + indent + "}";
    }
}
