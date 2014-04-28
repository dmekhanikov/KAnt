
public class Attribute {
    public String name;
    public String type;
    public String defaultValue;

    public Attribute(String name, String type, String defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public String toString() {
        String result = name + ": " + type;
        if (defaultValue != null) {
            result += " = " + defaultValue;
        }
        return result;
    }
}
