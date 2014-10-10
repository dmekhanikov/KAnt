package jetbrains.kant.translator;

import jetbrains.kant.translator.codeStructure.Context;
import jetbrains.kant.translator.codeStructure.Variable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static jetbrains.kant.gtcommon.GtcommonPackage.toCamelCase;
import static jetbrains.kant.gtcommon.constants.ConstantsPackage.getDSL_REFERENCE;

public class StringProcessor {
    public static String getType(String value) {
        if (value != null && (value.equals("true") || value.equals("false")
                || value.equals("yes") || value.equals("no"))) {
            return "Boolean";
        } else if (isInt(value)) {
            return "Int";
        } else if (isDouble(value)) {
            return "Double";
        } else {
            return "String";
        }
    }

    public static boolean isDouble(String string) { //Isn't the best solution but I don't know any better
        if (string == null || string.contains(" ")) {
            return false;
        }
        try {
            Double.parseDouble(string);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    public static boolean isInt(String string) {
        if (string == null || string.contains(" ")) {
            return false;
        }
        try {
            Integer.parseInt(string);
            return true;
        } catch(NumberFormatException e) {
            return false;
        }
    }

    public static boolean isStartOfTemplate(String string, int i) {
        return (string.charAt(i) == '$' || string.charAt(i) == '@') &&
                (i == 0 || string.charAt(i - 1) != string.charAt(i)) &&
                i + 1 < string.length() && string.charAt(i + 1) == '{';
    }

    public static String getPropertyName(String s) {
        if (!isStartOfTemplate(s, 0)) {
            return null;
        }
        int end = s.indexOf('}');
        if (end == -1) {
            return null;
        } else {
            return s.substring(2, end);
        }
    }

    /**
     * Get property name qualified if needed. Null
     *
     * @return property name or null if no property found
     */
    public static String processProperty(String s, Context context) {
        String name = getPropertyName(s);
        if (name == null) {
            return null;
        }
        boolean isAttribute = s.charAt(0) == '@';
        Variable functionArgument = context.getFunctionArgument(name);
        if (isAttribute && functionArgument == null || name.equals("$")) {
            return null;
        }
        String ccName;
        if (isAttribute) {
            ccName = functionArgument.getName();
        } else {
            ccName = toCamelCase(name);
            if (functionArgument != null) {
                ccName = context.getPackageName() + '.' + ccName;
            }
            if (context.getPropertyManager() != null) {
                context.getPropertyManager().readAccess(name);
            }
        }
        return ccName;
    }

    public static String processProperties(String value, Context context) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            if (isStartOfTemplate(value, i)) {
                String name = processProperty(value.substring(i), context);
                if (name == null) {
                    result.append(value.charAt(i)).append(value.charAt(i + 1));
                    i++;
                    continue;
                }
                i = value.indexOf('}', i);
                boolean isQualified = name.contains(".");
                if (isQualified || i + 1 < value.length() && Character.isJavaIdentifierPart(value.charAt(i + 1))) {
                    name = "${" + name + "}";
                } else {
                    name = "$" + name;
                }
                result.append(name);
            } else {
                result.append(value.charAt(i));
            }
        }
        return result.toString();
    }

    private static boolean isIdentifierStart(char c) {
        return Character.isLetter(c) || c == '_';
    }

    public static String escapeTemplates(String value) { //don't forget about @@ inside macrodefs
        StringBuilder result = new StringBuilder(value);
        for (int i = 0; i < result.length() - 1; i++) {
            if (result.charAt(i) == '$') {
                if (isIdentifierStart(result.charAt(i + 1))) {
                    result.insert(i + 1, "{\"$\"}");
                    i += 5;
                } else if (result.charAt(i + 1) == '$') {
                    if (i + 2 < result.length() &&
                            (isIdentifierStart(result.charAt(i + 2)) || result.charAt(i + 2) == '{')) {
                        result.replace(i + 1, i + 2, "{\"$\"}");
                    } else {
                        result.deleteCharAt(i);
                    }
                }
            }
        }
        return result.toString();
    }

    private static void escape(StringBuilder sb, char c) {
        String pattern = String.valueOf(c);
        for (int i = sb.indexOf(pattern); i != -1; i = sb.indexOf(pattern, i)) {
            sb.replace(i, i + 1, "\\" + pattern);
            i += 2;
        }
    }

    public static String prepareValue(String value, Context context, String type) {
        if (value == null) {
            return null;
        }
        if (type.startsWith(getDSL_REFERENCE())) {
            String name = toCamelCase(value);
            context.referenceAccess(name);
            if (context.hasFunctionArgument(value)) {
                name = context.getPackageName() + '.' + name;
            }
            return name;
        }
        try {
            switch (type) {
                case "Boolean":
                    if (value.equals("true") || value.equals("yes")) {
                        return "true";
                    } else if (value.equals("false") || value.equals("no")) {
                        return "false";
                    }
                    break;
                case "Char":
                    if (value.length() == 1) {
                        return "\'" + value + "\'";
                    }
                    break;
                case "Byte":
                    return String.valueOf(Byte.parseByte(value));
                case "Short":
                    return String.valueOf(Short.parseShort(value));
                case "Int":
                    return String.valueOf(Integer.parseInt(value));
                case "Float":
                    return String.valueOf(Float.parseFloat(value));
                case "Long":
                    return String.valueOf(Long.parseLong(value));
                case "Double":
                    return String.valueOf(Double.parseDouble(value));
            }
        } catch (NumberFormatException ignore) {}
        String pattern = "[$@]\\{([^\\{]+)\\}";
        Matcher matcher = Pattern.compile(pattern).matcher(value);
        if (matcher.matches()) {
            boolean isAttribute = value.charAt(0) == '@';
            String propName = matcher.group(1);
            String propCCName = processProperty(value, context);
            if (propCCName != null) {
                String propType = null;
                Variable functionArgument = context.getFunctionArgument(propName);
                if (isAttribute) {
                    if (functionArgument != null) {
                        propType = functionArgument.getType();
                    }
                } else if (context.getPropertyManager() != null) {
                    propType = context.getPropertyManager().getPropType(propName);
                }
                if (propType == null) {
                    propType = "String";
                }
                if (type.equals(propType)) {
                    return propCCName;
                } else if (type.equals("Char") && propType.equals("String")) {
                    return propCCName + "[0]";
                } else if (type.equals("String")) {
                    return propCCName + ".toString()";
                } else {
                    return propCCName + ".to" + type + "()";
                }
            }
        }
        StringBuilder result = new StringBuilder();
        result.append(processProperties(escapeTemplates(value), context));
        escape(result, '\\');
        escape(result, '\"');
        result.insert(0, "\"");
        result.append("\"");
        return result.toString();
    }
}
