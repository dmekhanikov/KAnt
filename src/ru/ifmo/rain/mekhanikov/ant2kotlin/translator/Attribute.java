package ru.ifmo.rain.mekhanikov.ant2kotlin.translator;

import static ru.ifmo.rain.mekhanikov.MekhanikovPackage.escapeKeywords;

public class Attribute {
    private String name;
    private String type;
    private String defaultValue;

    public Attribute(String name, String type, String defaultValue) {
        this.name = escapeKeywords(name);
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDefaultValue(PropertyManager propertyManager) {
        return StringProcessor.prepareValue(defaultValue, propertyManager);
    }

    public String toString(boolean includeType, PropertyManager propertyManager) {
        StringBuilder result = new StringBuilder(name);
        if (includeType && type != null) {
            result.append(": ").append(type);
        }
        if (defaultValue != null) {
            result.append(" = ");
            if (name.equals("refid") || name.endsWith("pathref")) {
                result.append(StringProcessor.toCamelCase(defaultValue));
            } else {
                result.append(StringProcessor.prepareValue(defaultValue, propertyManager));
            }
        }
        return result.toString();
    }

    @Override
    public String toString() {
        return toString(false, null);
    }
}
