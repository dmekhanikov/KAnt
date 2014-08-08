package jetbrains.kant.translator;

import static jetbrains.kant.KantPackage.toCamelCase;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class Macrodef extends Wrapper {
    private String macrodefName;

    public Macrodef(Attributes attrs) throws SAXException {
        super("macrodef", null, null);
        macrodefName = attrs.getValue("name");
        if (macrodefName == null) {
            throw new SAXException("Macrodef should have a name");
        }
        if (attrs.getLength() != 1) {
            throw new SAXException("Illegal attributes for macrodef");
        }
        macrodefName = toCamelCase(macrodefName);
    }

    public String toString(PropertyManager propertyManager) {
        return indent + "fun DSLTaskContainer." + macrodefName + renderAttributes(true, propertyManager) + " {\n"
                + renderChildren(propertyManager) + "\n"
                + indent + "}";
    }
}
