package jetbrains.kant.translator.wrappers;

import static jetbrains.kant.gtcommon.GtcommonPackage.toCamelCase;
import static jetbrains.kant.gtcommon.constants.ConstantsPackage.getDSL_TARGET_FUNCTION;

import jetbrains.kant.translator.codeStructure.Context;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class Target extends Wrapper {
    private String targetName;

    private String[] depends;

    public Target(Attributes attributes, Context context) throws SAXException {
        super(getDSL_TARGET_FUNCTION(), null, context);
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
    public String toString() {
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
        context.getImportManager().addImport(getDSL_TARGET_FUNCTION());
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
            renderChildren(result);
            result.append("\n").append(indent).append("}");
        } else {
            result.append(" {}");
        }
        return result.toString();
    }
}
