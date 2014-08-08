package jetbrains.kant.translator;

import jetbrains.kant.generator.DSLFunction;
import static jetbrains.kant.generator.GeneratorPackage.*;
import static jetbrains.kant.KantPackage.toCamelCase;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.util.ArrayList;
import java.util.List;

public class Wrapper {
    protected final String TAB = Translator.TAB;

    protected String name;
    protected DSLFunction constructor; //If null then it is considered a constructor of a task-container and all attributes are casted to String
    protected String indent = "";
    protected String id;
    protected List<Wrapper> children = new ArrayList<>();
    protected List<Attribute> attributes;
    protected Wrapper parent;
    public Wrapper(String name, Attributes attributes, DSLFunction constructor) {
        this.name = name;
        this.attributes = new ArrayList<>();
        this.constructor = constructor;
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                String attrName = attributes.getQName(i);
                String attrVal = attributes.getValue(i);
                if (attrName.equals("id")) {
                    id = attrVal;
                } else {
                    addAttribute(attrName, attrVal);
                }
            }
        }
    }

    public Wrapper(Wrapper wrapper) {
        name = wrapper.name;
        constructor = wrapper.constructor;
        indent = wrapper.indent;
        children = wrapper.children;
        attributes = wrapper.attributes;
        parent = wrapper.parent;
    }

    public void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }

    public void addAttribute(String name, String defaultValue) {
        String exactName = null;
        String type = null;
        if (constructor != null) {
            type = constructor.getAttributeType(name);
            exactName = constructor.getAttributeName(name);
        }
        if (exactName != null) {
            name = exactName;
        }
        if (type == null) {
            type = "String";
        }
        addAttribute(new Attribute(name, type, defaultValue));
    }

    public void setIndent(String indent) {
        this.indent = indent;
    }

    public Wrapper addChild(Wrapper child) throws SAXException {
        child.setIndent(indent + TAB);
        child.setParent(this);
        children.add(child);
        return child;
    }

    public Wrapper getParent() {
        return parent;
    }

    public void setParent(Wrapper parent) {
        this.parent = parent;
    }

    public DSLFunction getConstructor() {
        return constructor;
    }

    public String getDSLClassName() {
        if (name.equals("project")) {
            return getDSL_PROJECT();
        } else  if (name.equals("target")) {
            return getDSL_TARGET();
        } else if (constructor == null) {
            return getDSL_TASK_CONTAINER();
        } else {
            return constructor.getInitReceiver();
        }
    }

    protected String renderAttributes(boolean includeTypes, PropertyManager propertyManager) {
        StringBuilder result = new StringBuilder("(");
        for (int i = 0; i < attributes.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(attributes.get(i).toString(includeTypes, propertyManager));
        }
        result.append(')');
        return result.toString();
    }

    protected String renderChildren(PropertyManager propertyManager) {
        StringBuilder result = new StringBuilder();
        if (!children.isEmpty()) {
            result.append(children.get(0).toString(propertyManager));
            for (Wrapper child : children.subList(1, children.size())) {
                result.append("\n").append(child.toString(propertyManager));
            }
        }
        return result.toString();
    }

    @Override
    public String toString() {
        return toString(null);
    }

    public String toString(PropertyManager propertyManager) {
        StringBuilder result = new StringBuilder(indent);
        if (id != null) {
            result.append("val ").append(toCamelCase(id)).append(" = ");
        }
        result.append(toCamelCase(name));
        if (!attributes.isEmpty()) {
            result.append(renderAttributes(false, propertyManager));
        } else if (children.isEmpty()) {
            result.append("()");
        }
        if (!children.isEmpty()) {
            result.append(" {\n");
            result.append(renderChildren(propertyManager));
            result.append("\n").append(indent).append("}");
        }
        return result.toString();
    }
}
