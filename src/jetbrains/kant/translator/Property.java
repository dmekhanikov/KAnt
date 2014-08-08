package jetbrains.kant.translator;

import jetbrains.kant.generator.DSLFunction;
import static jetbrains.kant.KantPackage.toCamelCase;
import org.xml.sax.Attributes;

public class Property extends Wrapper {
    private String propName;
    private String propVal;
    private String propType;
    boolean includeDefVal = false;
    private boolean isMutable;

    public Property(Attributes attributes, DSLFunction constructor) {
        super("property", attributes, constructor);
        propName = attributes.getValue("name");
        propVal = attributes.getValue("value");
        setPropType();
    }

    public Property(String propName, String propVal, DSLFunction constructor) {
        super("property", null, constructor);
        this.propName = propName;
        this.propVal = propVal;
        setPropType();
    }

    public String getName() {
        return propName;
    }

    private void setPropType() {
        if (propVal != null) {
            propType = StringProcessor.getType(propVal);
        } else {
            propType = "String";
        }
    }

    public String getPropType() {
        return propType;
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
            return indent + toCamelCase(propName) + " = " + StringProcessor.prepareValue(propVal, propertyManager, propType);
        } else {
            return super.toString(propertyManager);
        }
    }

    public String getDeclaration(PropertyManager propertyManager) {
        StringBuilder result = new StringBuilder();
        if (isMutable) {
            result.append("var");
        } else {
            result.append("val");
        }
        String ccName = toCamelCase(propName);
        result.append(" ").append(ccName).append(" by ").append(propType).append("Property");
        if (!propName.equals(ccName)) {
            result.append("(\"").append(propName).append("\")");
        } else {
            result.append("()");
        }
        if (includeDefVal && propVal != null) {
            result.append(" { ").append(StringProcessor.prepareValue(propVal, propertyManager, propType)).append(" }");
        }
        return result.toString();
    }
}
