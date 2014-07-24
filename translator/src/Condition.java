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
    public String toString() {
        String result;
        switch (name) {
            case "not":
                return "!" + children.get(0).toString();
            case "and":
                result = children.get(0).toString() + " && " + children.get(1).toString();
                if (parent != null && parent.name.equals("not")) {
                    result = "(" + result + ")";
                }
                return result;
            case "or":
                result = children.get(0).toString() + " or " + children.get(1).toString();
                if (parent != null && (parent.name.equals("not") || parent.name.equals("and"))) {
                    result = "(" + result + ")";
                }
                return result;
            case "xor":
                result = children.get(0).toString() + " xor " + children.get(1).toString();
                if (parent != null && (parent.name.equals("not") || parent.name.equals("and"))) {
                    result = "(" + result + ")";
                }
                return result;
            case "isset":
                return "propertyIsSet(" + attributes.get(0).getDefaultValue() + ")";
            case "istrue":
                return attributes.get(0).getDefaultValue();
            case "isfalse":
                return "!" + attributes.get(0).getDefaultValue();
            default:
                return super.toString();
        }
    }
}
