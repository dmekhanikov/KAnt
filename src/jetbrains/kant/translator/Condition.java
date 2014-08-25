package jetbrains.kant.translator;

import jetbrains.kant.gtcommon.ImportManager;
import org.xml.sax.SAXException;

public class Condition extends Wrapper {
    public Condition(Wrapper wrapper) {
        super(wrapper);
    }

    @Override
    public Wrapper addChild(Wrapper child) throws SAXException {
        child.setParent(this);
        Condition childCondition = new Condition(child);
        children.add(childCondition);
        return childCondition;
    }

    @Override
    public String getDSLClassName() {
        if (parent != null && (name.equals("not") || name.equals("and") || name.equals("or") || name.equals("xor"))) {
            return parent.getDSLClassName();
        } else {
            return super.getDSLClassName();
        }
    }

    @Override
    public String toString(PropertyManager propertyManager, ImportManager importManager) {
        String result;
        switch (name) {
            case "not":
                return "!" + children.get(0).toString(propertyManager, importManager);
            case "and":
                result = children.get(0).toString(propertyManager, importManager) + " && " + children.get(1).toString(propertyManager, importManager);
                if (parent != null && "not".equals(parent.name)) {
                    result = "(" + result + ")";
                }
                return result;
            case "or":
                result = children.get(0).toString(propertyManager, importManager) + " || " + children.get(1).toString(propertyManager, importManager);
                if (parent != null && ("not".equals(parent.name) || "and".equals(parent.name))) {
                    result = "(" + result + ")";
                }
                return result;
            case "xor":
                result = children.get(0).toString(propertyManager, importManager) + " ^ " + children.get(1).toString(propertyManager, importManager);
                if (parent != null && ("not".equals(parent.name) || "and".equals(parent.name))) {
                    result = "(" + result + ")";
                }
                return result;
            case "istrue":
                return attributes.get(0).getDefaultValue(propertyManager);
            case "isfalse":
                return "!" + attributes.get(0).getDefaultValue(propertyManager);
            default:
                return super.toString(propertyManager, importManager);
        }
    }
}
