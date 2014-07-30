import org.xml.sax.Attributes;

public class Property extends Wrapper {
    private String propName;
    private String propVal;
    boolean includeDefVal = false;

    private boolean isMutable;

    public Property(Attributes attributes) {
        super("property", attributes);
        propName = attributes.getValue("name");
        propVal = attributes.getValue("value");
    }

    public Property(String propName, String propVal) {
        super("property", null);
        this.propName = propName;
        this.propVal = propVal;
    }

    public void setIncludeDefVal(boolean includeDefVal) {
        this.includeDefVal = includeDefVal;
    }

    public void setMutable(boolean isMutable) {
        this.isMutable = isMutable;
    }

    @Override
    public String toString(PropertyManager propertyManager) {
        if (propName != null && propVal != null && attributes.size() <= 2) {
            return indent + StringProcessor.toCamelCase(propName) + " = " + StringProcessor.prepareValue(propVal, propertyManager);
        } else {
            return super.toString(propertyManager);
        }
    }

    public String getName() {
        return propName;
    }

    public String getDeclaration(PropertyManager propertyManager) {
        StringBuilder result = new StringBuilder();
        if (isMutable) {
            result.append("var");
        } else {
            result.append("val");
        }
        String ccName = StringProcessor.toCamelCase(propName);
        String propType;
        if (propVal != null) {
            propType = StringProcessor.getType(propVal);
        } else {
            propType = "String";
        }
        result.append(" ").append(ccName).append(" by ").append(propType).append("Property");
        if (!propName.equals(ccName)) {
            result.append("(\"").append(propName).append("\")");
        } else {
            result.append("()");
        }
        if (includeDefVal && propVal != null) {
            result.append(" { ").append(StringProcessor.prepareValue(propVal, propertyManager)).append(" }");
        }
        return result.toString();
    }
}
