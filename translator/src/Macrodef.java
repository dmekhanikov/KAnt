import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;

public class Macrodef {
    String name;
    List<Attribute> attributes = new ArrayList<>();

    public Macrodef(String name) {
        this.name = name;
    }

    public Macrodef(Attributes attrs) throws SAXException {
        name = attrs.getValue("name");
        if (name == null) {
            throw new SAXException("Macrodef should have a name");
        }
        if (attrs.getLength() != 1) {
            throw new SAXException("Illegal attributes for macrodef");
        }
        name = StringProcessor.toCamelCase(name);
    }

    public void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }

    public void addAttribute(String name, String defaultValue) {
        name = StringProcessor.toCamelCase(name);
        String type;
        if (defaultValue != null && (defaultValue.equals("true") || defaultValue.equals("false"))) {
            type = "Boolean";
        } else {
            type = "String";
        }
        defaultValue = StringProcessor.prepareValue(defaultValue);
        addAttribute(new Attribute(name, type, defaultValue));
    }

    public String toString() {
        StringBuilder result = new StringBuilder("fun DSLTaskContainer." + name + "(");
        for (int i = 0; i < attributes.size(); i++) {
            if (i != 0) {
                result.append(", ");
            }
            result.append(attributes.get(i).toString());
        }
        result.append(")");
        return result.toString();
    }
}
