package jetbrains.kant.translator;

import java.util.ArrayList;
import java.util.List;

import jetbrains.kant.generator.DSLFunction;
import jetbrains.kant.gtcommon.ImportManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import static jetbrains.kant.gtcommon.StringsKt.toCamelCase;
import static jetbrains.kant.gtcommon.constants.ConstantsKt.DSL_PROJECT;
import static jetbrains.kant.gtcommon.constants.ConstantsKt.DSL_PROJECT_FUNCTION;
import static jetbrains.kant.gtcommon.constants.ConstantsKt.DSL_TARGET;
import static jetbrains.kant.gtcommon.constants.ConstantsKt.DSL_TARGET_FUNCTION;
import static jetbrains.kant.gtcommon.constants.ConstantsKt.DSL_TASK_CONTAINER;

public class Wrapper {
    protected final String TAB = Translator.TAB;

    protected String name;
    protected DSLFunction constructor; //If null then it is considered a constructor of a task-container and all attributes are casted to String
    protected String indent = "";
    protected String id;
    protected List<Wrapper> children = new ArrayList<>();
    protected List<DSLAttribute> attributes;
    protected Wrapper parent;
    private Text text;

    private Wrapper(String name, DSLFunction constructor, Attributes attributes) {
        this.constructor = constructor;
        if (constructor != null) {
            this.name = constructor.getName();
        } else {
            this.name = name;
        }
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

    public Wrapper(String name, Attributes attributes) {
        this(name, null, attributes);
    }

    public Wrapper(DSLFunction constructor, Attributes attributes) {
        this(null, constructor, attributes);
    }

    public Wrapper(Wrapper wrapper) {
        name = wrapper.name;
        constructor = wrapper.constructor;
        indent = wrapper.indent;
        children = wrapper.children;
        attributes = wrapper.attributes;
        parent = wrapper.parent;
    }

    public void addAttribute(DSLAttribute attribute) {
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
        addAttribute(new DSLAttribute(name, type, defaultValue));
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

    public void addText(String string) throws SAXException {
        if (text == null) {
            text = new Text();
            addChild(text);
        }
        text.append(string);
    }

    public List<DSLAttribute> getAttributes() {
        return attributes;
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

    public String getConstructorQName() {
        if (constructor != null) {
            return constructor.getPkg() + "." + constructor.getName();
        } else if (name.equals("project")) {
            return DSL_PROJECT_FUNCTION;
        } else if (name.equals("target")) {
            return DSL_TARGET_FUNCTION;
        } else {
            return null;
        }
    }

    public String getDSLClassName() {
        if (DSL_PROJECT_FUNCTION.equals(name)) {
            return DSL_PROJECT;
        } else if (DSL_TARGET_FUNCTION.equals(name)) {
            return DSL_TARGET;
        } else if (constructor == null) {
            return DSL_TASK_CONTAINER;
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

    protected String renderChildren(PropertyManager propertyManager, ImportManager importManager) {
        StringBuilder result = new StringBuilder();
        if (!children.isEmpty()) {
            result.append(children.get(0).toString(propertyManager, importManager));
            for (Wrapper child : children.subList(1, children.size())) {
                result.append("\n").append(child.toString(propertyManager, importManager));
            }
        }
        return result.toString();
    }

    public String toString(PropertyManager propertyManager, ImportManager importManager) {
        StringBuilder result = new StringBuilder(indent);
        if (id != null) {
            result.append("val ").append(toCamelCase(id)).append(" = ");
        }
        importManager.addImport(getConstructorQName());
        result.append(toCamelCase(name));
        if (!attributes.isEmpty()) {
            result.append(renderAttributes(false, propertyManager));
        } else if (children.isEmpty()) {
            result.append("()");
        }
        if (!children.isEmpty()) {
            result.append(" {\n");
            result.append(renderChildren(propertyManager, importManager));
            result.append("\n").append(indent).append("}");
        }
        return result.toString();
    }
}
