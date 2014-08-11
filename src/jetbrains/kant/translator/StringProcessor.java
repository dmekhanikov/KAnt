package jetbrains.kant.translator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static jetbrains.kant.KantPackage.toCamelCase;
import static jetbrains.kant.generator.GeneratorPackage.*;

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

    public static String processProperties(String value, PropertyManager propertyManager) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            if (isStartOfTemplate(value, i)) {
                boolean isAttribute = value.charAt(i) == '@';
                StringBuilder propertyName = new StringBuilder();
                for (i += 2; i < value.length() && value.charAt(i) != '}'; i++) {
                    propertyName.append(value.charAt(i));
                }
                String name = propertyName.toString();
                if (isAttribute && (propertyManager == null || !propertyManager.containsAttribute(name))) {
                    result.append("@{").append(name).append("}");
                } else if (name.equals("\"$\"")) {
                    result.append("${\"$\"}");
                } else {
                    String ccName = toCamelCase(name);
                    if (propertyManager != null) {
                        if (isAttribute) {
                            name = propertyManager.getExactAttributeName(name);
                            ccName = toCamelCase(name);
                        } else {
                            propertyManager.readAccess(propertyName.toString());
                        }
                    }
                    if (i + 1 < value.length() && Character.isJavaIdentifierPart(value.charAt(i + 1))) {
                        ccName = "${" + ccName + "}";
                    } else {
                        ccName = "$" + ccName;
                    }
                    result.append(ccName);
                }
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

    public static String prepareValue(String value, PropertyManager propertyManager, String type) {
        if (value == null) {
            return null;
        }
        if (type.startsWith(getDSL_REFERENCE())) {
            return toCamelCase(value);
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
            String propCCName = processProperties(value, propertyManager).substring(1);
            if (propCCName.charAt(0) != '{') {
                String propType = null;
                if (propertyManager != null) {
                    if (isAttribute) {
                        propType = propertyManager.getAttributeType(propName);
                    } else {
                        propType = propertyManager.getPropType(propName);
                    }
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
        result.append(processProperties(escapeTemplates(value), propertyManager));
        escape(result, '\\');
        escape(result, '\"');
        result.insert(0, "\"");
        result.append("\"");
        return result.toString();
    }
}
