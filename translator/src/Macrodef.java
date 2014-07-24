import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;

public class Macrodef extends Wrapper {
    private String macrodefName;

    public Macrodef(Attributes attrs) throws SAXException {
        super("macrodef", null);
        macrodefName = attrs.getValue("name");
        if (macrodefName == null) {
            throw new SAXException("Macrodef should have a name");
        }
        if (attrs.getLength() != 1) {
            throw new SAXException("Illegal attributes for macrodef");
        }
        macrodefName = StringProcessor.toCamelCase(macrodefName);
    }

    public String toString() {
        StringBuilder result = new StringBuilder(indent + "fun DSLTaskContainer." + macrodefName);
        result.append(renderAttributes(true));
        result.append(" {\n");
        result.append(renderChildren()).append("\n");
        result.append(indent).append("}");
        return result.toString();
    }
}
