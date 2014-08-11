package jetbrains.kant.translator;

import jetbrains.kant.AntAttribute;
import static jetbrains.kant.KantPackage.toCamelCase;

public class DSLAttribute extends AntAttribute {
    private String defaultValue;

    public DSLAttribute(String name, String typeName, String defaultValue) {
        super(name, typeName);
        this.defaultValue = defaultValue;
    }

    public String getDefaultValue(PropertyManager propertyManager) {
        return StringProcessor.prepareValue(defaultValue, propertyManager, getTypeName());
    }

    public String toString(boolean includeType, PropertyManager propertyManager) {
        StringBuilder result = new StringBuilder(toCamelCase(getName()));
        if (includeType) {
            result.append(": ").append(getTypeName());
        }
        if (defaultValue != null) {
            result.append(" = ");
            result.append(getDefaultValue(propertyManager));
        }
        return result.toString();
    }
}
