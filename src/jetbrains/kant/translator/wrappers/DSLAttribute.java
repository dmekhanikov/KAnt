package jetbrains.kant.translator.wrappers;

import jetbrains.kant.gtcommon.AntAttribute;
import jetbrains.kant.translator.StringProcessor;
import jetbrains.kant.translator.codeStructure.Context;

import static jetbrains.kant.gtcommon.GtcommonPackage.toCamelCase;

public class DSLAttribute extends AntAttribute {
    private String defaultValue;

    public DSLAttribute(String name, String typeName, String defaultValue) {
        super(name, typeName);
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue(Context context) {
        return StringProcessor.prepareValue(defaultValue, context, getTypeName());
    }

    public String toString(boolean includeType, Context context) {
        StringBuilder result = new StringBuilder(toCamelCase(getName()));
        if (includeType) {
            result.append(": ").append(getTypeName());
        }
        if (defaultValue != null) {
            result.append(" = ");
            result.append(getDefaultValue(context));
        }
        return result.toString();
    }
}
