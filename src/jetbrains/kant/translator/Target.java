package jetbrains.kant.translator;

import jetbrains.kant.gtcommon.ImportManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import static jetbrains.kant.gtcommon.StringsKt.toCamelCase;
import static jetbrains.kant.gtcommon.constants.ConstantsKt.DSL_TARGET_FUNCTION;

public class Target extends Wrapper {
    private String targetName;

    private String[] depends;

    public Target(Attributes attributes) throws SAXException {
        super(DSL_TARGET_FUNCTION, null);
        targetName = attributes.getValue("name");
        if (targetName == null) {
            throw new SAXException("Target should have a name");
        }
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
        if (((Project) parent).getDefaultTarget().equals(targetName)) {
            result.append(indent).append("[default]\n");
        }
        String fieldName = toCamelCase(targetName);
        result.append(indent).append("val ").append(fieldName);
        result.append(" = target");
        boolean firstArgument = true;
        if (!fieldName.equals(targetName)) {
            result.append("(\"").append(targetName).append("\"");
            firstArgument = false;
        }
        importManager.addImport(DSL_TARGET_FUNCTION);
        if (depends != null) {
            for (String depend : depends) {
                if (firstArgument) {
                    firstArgument = false;
                    result.append("(");
                } else {
                    result.append(", ");
                }
                result.append("::").append(toCamelCase(depend));
            }
        }
        if (!firstArgument) {
            result.append(")");
        }
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
