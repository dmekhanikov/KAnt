package jetbrains.kant.translator;

import jetbrains.kant.generator.DSLFunction;
import jetbrains.kant.gtcommon.ImportManager;
import org.xml.sax.Attributes;

import static jetbrains.kant.gtcommon.StringsKt.toCamelCase;
import static jetbrains.kant.gtcommon.constants.ConstantsKt.DSL_PROPERTIES_PACKAGE;

public class Property extends Wrapper {
    private String propName;
    private String propVal;
    private String propType;
    private boolean includeDefVal = false;
    private boolean isMutable;

    public Property(Attributes attributes, DSLFunction constructor) {
        super(constructor, attributes);
        propName = attributes.getValue("name");
        propVal = attributes.getValue("value");
        setPropType();
    }

    public Property(Attributes attributes) {
        super("property", attributes);
        propName = attributes.getValue("name");
        propVal = attributes.getValue("value");
        setPropType();
    }

    public Property(String propName, String propVal) {
        super((String) null, null);
        this.propName = propName;
        this.propVal = propVal;
        setPropType();
    }

    public Property(String propName, String propVal, DSLFunction constructor) {
        super(constructor, null);
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
    public String toString(PropertyManager propertyManager, ImportManager importManager) {
        if (propName != null && propVal != null && attributes.size() <= 2) {
            return indent + toCamelCase(propName) + " = " + StringProcessor.prepareValue(propVal, propertyManager, propType);
        } else {
            return super.toString(propertyManager, importManager);
        }
    }

    public String getDeclaration(PropertyManager propertyManager, ImportManager importManager) {
        StringBuilder result = new StringBuilder();
        if (isMutable) {
            result.append("var");
        } else {
            result.append("val");
        }
        String ccName = toCamelCase(propName);
        String propDelegateType = importManager.shorten(DSL_PROPERTIES_PACKAGE + "." + propType + "Property");
        result.append(" ").append(ccName).append(" by ").append(propDelegateType);
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
