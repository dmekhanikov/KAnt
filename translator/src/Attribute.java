
public class Attribute {
    private String name;
    private String type;
    private String defaultValue;

    public Attribute(String name, String type, String defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String toString(boolean includeType) {
        StringBuilder result = new StringBuilder(name);
        if (includeType && type != null) {
            result.append(": ").append(type);
        }
        if (defaultValue != null) {
            result.append(" = ").append(defaultValue);
        }
        return result.toString();
    }

    @Override
    public String toString() {
        return toString(false);
    }
}
