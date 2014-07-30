package ru.ifmo.rain.mekhanikov.ant2kotlin.translator;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.util.ArrayList;
import java.util.List;

public class Wrapper {
    protected final String TAB = Translator.TAB;

    protected String name;
    protected String indent = "";
    protected String id;
    protected List<Wrapper> children = new ArrayList<>();
    protected List<Attribute> attributes;
    protected Wrapper parent;

    public Wrapper(String name, Attributes attributes) {
        this.name = name;
        this.attributes = new ArrayList<>();
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
        indent = wrapper.indent;
        children = wrapper.children;
        attributes = wrapper.attributes;
        parent = wrapper.parent;
    }

    public void addAttribute(Attribute attribute) {
        attributes.add(attribute);
    }

    public void addAttribute(String name, String defaultValue) {
        name = StringProcessor.toCamelCase(name);
        String type = StringProcessor.getType(defaultValue);
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
            result.append("val ").append(StringProcessor.toCamelCase(id)).append(" = ");
        }
        result.append(StringProcessor.toCamelCase(name));
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
