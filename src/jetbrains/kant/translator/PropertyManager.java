package jetbrains.kant.translator;

import jetbrains.kant.ImportManager;
import java.util.HashMap;

public class PropertyManager {
    private boolean isDeclaring = true;
    private HashMap<String, Property> pool = new HashMap<>();

    @Override
    public String toString() {
        return toString(null);
    }

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

    public boolean isDeclaring() {
        return isDeclaring;
    }

    public void finishDeclaring() {
        isDeclaring = false;
    }
}
