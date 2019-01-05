package jetbrains.kant.translator;

import jetbrains.kant.gtcommon.ImportManager;

import java.util.HashMap;

public class PropertyManager {
    private boolean isDeclaring = true;
    private HashMap<String, Property> pool = new HashMap<>();
    private HashMap<String, DSLAttribute> attributes = new HashMap<>();

    public String toString(ImportManager importManager) {
        StringBuilder result = new StringBuilder();
        for (Property property : pool.values()) {
            result.append(property.getDeclaration(null, importManager)).append("\n");
        }
        return result.toString();
    }

    public void writeAccess(Property property) {
        String propName = property.getName();
        if (propName != null && isDeclaring()) {
            property.setIncludeDefVal(true);
            pool.put(propName, property);
        } else {
            Property oldProperty = pool.get(propName);
            if (oldProperty != null) {
                oldProperty.setMutable(true);
            } else if (propName != null) {
                property.setMutable(true);
                pool.put(propName, property);
            }
        }
    }

    public void readAccess(String name) {
        if (!pool.containsKey(name)) {
            Property property = new Property(name, null);
            pool.put(name, property);
        }
    }

    public String getPropType(String propName) {
        Property prop = pool.get(propName);
        if (prop != null) {
            return prop.getPropType();
        } else {
            return null;
        }
    }

    public void addAttribute(DSLAttribute attribute) {
        attributes.put(attribute.getName().toLowerCase(), attribute);
    }

    public void clearAttributes() {
        attributes.clear();
    }

    public boolean containsAttribute(String attrName) {
        return attributes.containsKey(attrName);
    }

    public DSLAttribute getAttribute(String attrName) {
        return attributes.get(attrName.toLowerCase());
    }

    public String getExactAttributeName(String attrName) {
        DSLAttribute attr = getAttribute(attrName);
        if (attr != null) {
            return attr.getName();
        } else {
            return attrName;
        }
    }

    public String getAttributeType(String attrName) {
        DSLAttribute attr = getAttribute(attrName);
        if (attr != null) {
            return attr.getTypeName();
        } else {
            return null;
        }
    }

    public boolean isDeclaring() {
        return isDeclaring;
    }

    public void finishDeclaring() {
        isDeclaring = false;
    }
}
