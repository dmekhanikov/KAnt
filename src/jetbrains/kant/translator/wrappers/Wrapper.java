package jetbrains.kant.translator.wrappers;

import jetbrains.kant.generator.DSLFunction;

import static jetbrains.kant.gtcommon.GtcommonPackage.toCamelCase;
import static jetbrains.kant.gtcommon.constants.ConstantsPackage.*;

import jetbrains.kant.translator.Translator;
import jetbrains.kant.translator.codeStructure.Context;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.util.ArrayList;
import java.util.List;

public class Wrapper {
    protected final String TAB = Translator.TAB;

    protected String name;
    protected DSLFunction constructor; //If null then it is considered a constructor of a task-container and all attributes are casted to String
    protected Context context;
    protected String indent = "";
    protected String id;
    protected List<Wrapper> children = new ArrayList<>();
    protected List<DSLAttribute> attributes;
    protected Wrapper parent;
    private Text text;

    private Wrapper(String name, DSLFunction constructor, Attributes attributes, Context context) {
        this.constructor = constructor;
        this.context = context;
        if (context != null) {
            context.setWrapper(this);
        }
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

    public Wrapper(String name, Attributes attributes, Context context) {
        this(name, null, attributes, context);
    }

    public Wrapper(DSLFunction constructor, Attributes attributes, Context context) {
        this(null, constructor, attributes, context);
    }

    public Wrapper(Wrapper wrapper) {
        name = wrapper.name;
        constructor = wrapper.constructor;
        context = wrapper.context;
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

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
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

    public void addText(String string) throws SAXException{
        if (text == null) {
            text = new Text(context);
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
            return getDSL_PROJECT_FUNCTION();
        } else if (name.equals("target")) {
            return getDSL_TARGET_FUNCTION();
        } else {
            return null;
        }
    }

    public String getDSLClassName() {
        if (getDSL_PROJECT_FUNCTION().equals(name)) {
            return getDSL_PROJECT();
        } else  if (getDSL_TARGET_FUNCTION().equals(name)) {
            return getDSL_TARGET();
        } else if (constructor == null) {
            return getDSL_TASK_CONTAINER();
        } else {
            return constructor.getInitReceiver();
        }
    }

    protected String renderAttributes(boolean includeTypes, Context context) {
        StringBuilder result = new StringBuilder("(");
        for (int i = 0; i < attributes.size(); i++) {
            if (i > 0) {
                result.append(", ");
            }
            result.append(attributes.get(i).toString(includeTypes, context));
        }
        result.append(')');
        return result.toString();
    }

    protected void renderChildren(StringBuilder sb) {
        if (!children.isEmpty()) {
            Wrapper first = children.get(0);
            first.getContext().setOffset(context.getOffset() + sb.length());
            sb.append(children.get(0).toString());
            for (Wrapper child : children.subList(1, children.size())) {
                child.getContext().setOffset(context.getOffset() + sb.length());
                sb.append("\n").append(child.toString());
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(indent);
        if (id != null) {
            String name = toCamelCase(id);
            String type;
            if (constructor != null) {
                type = constructor.getReturnType();
            } else {
                type = context.getImportManager().shorten(getDSL_REFERENCE());
            }
            context.getParent().addVariable(id, type, context.getOffset() + result.length());
            result.append("val ").append(name).append(" = ");
        }
        context.getImportManager().addImport(getConstructorQName());
        result.append(toCamelCase(name));
        if (!attributes.isEmpty()) {
            result.append(renderAttributes(false, context));
        } else if (children.isEmpty()) {
            result.append("()");
        }
        if (!children.isEmpty()) {
            result.append(" {\n");
            renderChildren(result);
            result.append("\n").append(indent).append("}");
        }
        return result.toString();
    }
}
