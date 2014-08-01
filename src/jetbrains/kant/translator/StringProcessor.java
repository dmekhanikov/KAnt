package jetbrains.kant.translator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jetbrains.kant.KantPackage.escapeKeywords;

public class StringProcessor {
    public static String toCamelCase(String name) {
        StringBuilder stringBuilder = new StringBuilder(name.toLowerCase());
        String separators = ".-_";
        for (int i = 0; i < separators.length(); i++) {
            String separator = String.valueOf(separators.charAt(i));
            for (int j = stringBuilder.indexOf(separator); j != -1; j = stringBuilder.indexOf(separator, j)) {
                stringBuilder.deleteCharAt(j);
                if (stringBuilder.length() > j) {
                    stringBuilder.setCharAt(j, Character.toUpperCase(stringBuilder.charAt(j)));
                }
            }
        }
        if (stringBuilder.charAt(0) == '\"' && stringBuilder.charAt(stringBuilder.length() - 1) == '\"') {
            stringBuilder.deleteCharAt(0);
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return escapeKeywords(stringBuilder.toString());
    }

    public static String getType(String value) {
        if (value != null && (value.equals("true") || value.equals("false")
                || value.equals("yes") || value.equals("no"))) {
            return "Boolean";
        } else {
            return "String";
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
                if (name.equals("\"$\"")) {
                    result.append("${\"$\"}");
                } else {
                    if (propertyManager != null && !isAttribute) {
                        propertyManager.readAccess(propertyName.toString());
                    }
                    name = toCamelCase(name);
                    if (i + 1 < value.length() && Character.isJavaIdentifierPart(value.charAt(i + 1))) {
                        name = "${" + name + "}";
                    } else {
                        name = "$" + name;
                    }
                    result.append(name);
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

    public static String prepareValue(String value, PropertyManager propertyManager) {
        if (value == null) {
            return null;
        }
        switch (value) {
            case "true":
            case "yes":
                return "true";
            case "false":
            case "no":
                return "false";
            default:
        }
        StringBuilder result = new StringBuilder("\"");
        result.append(processProperties(escapeTemplates(value), propertyManager));
        result.append("\"");
        for (int i = result.indexOf("\\"); i != -1; i = result.indexOf("\\", i)) {
            result.replace(i, i + 1, "\\\\");
            i += 2;
        }
        String pattern = "\"\\$(\\w*)\"";
        Matcher matcher = Pattern.compile(pattern).matcher(result);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return result.toString();
    }
}
