package jetbrains.kant.translator;

import static jetbrains.kant.gtcommon.GtcommonPackage.toCamelCase;
import static jetbrains.kant.gtcommon.constants.ConstantsPackage.getDSL_TARGET_FUNCTION;
import jetbrains.kant.gtcommon.ImportManager;
import org.xml.sax.Attributes;

public class Target extends Wrapper {
    private String targetName;

    private String[] depends;

    public Target(Attributes attributes) {
        super(getDSL_TARGET_FUNCTION(), null);
        targetName = attributes.getValue("name");
        String dependsString = attributes.getValue("depends");
        if (dependsString != null) {
            depends = dependsString.split(",");
            for (int i = 0; i < depends.length; i++) {
                depends[i] = depends[i].trim();
            }
        }
    }

    public String getTargetName() {
        return targetName;
    }

    public String[] getDepends() {
        return depends;
    }

    @Override
    public String toString(PropertyManager propertyManager, ImportManager importManager) {
        StringBuilder result = new StringBuilder();
        result.append(indent).append("val ").append(toCamelCase(targetName));
        result.append(" = target(\"").append(targetName).append("\"");
        importManager.addImport(getDSL_TARGET_FUNCTION());
        if (depends != null) {
            for (String depend : depends) {
                result.append(", ::").append(toCamelCase(depend));
            }
        }
        result.append(")");
        if (!children.isEmpty()) {
            result.append(" {\n");
            result.append(renderChildren(propertyManager, importManager));
            result.append("\n").append(indent).append("}");
        } else {
            result.append(" {}");
        }
        return result.toString();
    }
}
