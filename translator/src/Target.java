import org.xml.sax.Attributes;

public class Target extends Wrapper {
    private String targetName;

    private String[] depends;

    public Target(Attributes attributes) {
        super("target", null);
        targetName = attributes.getValue("name");
        String dependsString = attributes.getValue("depends");
        if (dependsString != null) {
            depends = dependsString.split(",");
        }
    }

    public String getTargetName() {
        return targetName;
    }

    public String[] getDepends() {
        return depends;
    }

    @Override
    public String toString(PropertyManager propertyManager) {
        StringBuilder result = new StringBuilder();
        result.append(indent).append("val ").append(StringProcessor.toCamelCase(targetName));
        result.append(" = target(\"").append(targetName).append("\"");
        if (depends != null) {
            for (String depend : depends) {
                result.append(", ").append(StringProcessor.toCamelCase(depend));
            }
        }
        result.append(")");
        if (!children.isEmpty()) {
            result.append(" {\n");
            result.append(renderChildren(propertyManager));
            result.append("\n").append(indent).append("}");
        } else {
            result.append(" {}");
        }
        return result.toString();
    }
}
