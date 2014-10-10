package jetbrains.kant.translator.wrappers;

import jetbrains.kant.generator.DSLFunction;
import static jetbrains.kant.gtcommon.GtcommonPackage.toCamelCase;
import static jetbrains.kant.gtcommon.constants.ConstantsPackage.getDSL_PROPERTIES_PACKAGE;

import jetbrains.kant.translator.StringProcessor;
import jetbrains.kant.translator.codeStructure.Context;
import org.xml.sax.Attributes;

public class Property extends Wrapper {
    private String propName;
    private String propVal;
    private String propType;
    boolean includeDefVal = false;
    private boolean isMutable;

    public Property(Attributes attributes, DSLFunction constructor, Context context) {
        super(constructor, attributes, context);
        propName = attributes.getValue("name");
        propVal = attributes.getValue("value");
        setPropType();
    }

    public Property(Attributes attributes, Context context) {
        super("property", attributes, context);
        propName = attributes.getValue("name");
        propVal = attributes.getValue("value");
        setPropType();
    }

    public Property(String propName, String propVal, Context context) {
        super((String) null, null, context);
        this.propName = propName;
        this.propVal = propVal;
        setPropType();
    }

    public Property(String propName, String propVal, DSLFunction constructor, Context context) {
        super(constructor, null, context);
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
    public String toString() {
        if (propName != null && propVal != null && attributes.size() <= 2) {
            return indent + toCamelCase(propName) + " = " + StringProcessor.prepareValue(propVal, context, propType);
        } else {
            return super.toString();
        }
    }

    public String getDeclaration(Context context) {
        StringBuilder result = new StringBuilder();
        if (isMutable) {
            result.append("var");
        } else {
            result.append("val");
        }
        String ccName = toCamelCase(propName);
        String propDelegateType = context.getImportManager().shorten(getDSL_PROPERTIES_PACKAGE() + "." + propType + "Property");
        result.append(" ").append(ccName).append(" by ").append(propDelegateType);
        if (!propName.equals(ccName)) {
            result.append("(\"").append(propName).append("\")");
        } else {
            result.append("()");
        }
        if (includeDefVal && propVal != null) {
            result.append(" { ").append(StringProcessor.prepareValue(propVal, context, propType)).append(" }");
        }
        return result.toString();
    }
}
