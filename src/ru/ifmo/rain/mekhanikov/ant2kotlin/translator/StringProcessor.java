package ru.ifmo.rain.mekhanikov.ant2kotlin.translator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.ifmo.rain.mekhanikov.MekhanikovPackage.escapeKeywords;

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

    public static String processProperties(String value, PropertyManager propertyManager) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            if (i + 1 < value.length() &&
                    (value.charAt(i) == '$' || value.charAt(i) == '@') &&
                    value.charAt(i + 1) == '{') {
                boolean isAttribute = value.charAt(i) == '@';
                StringBuilder propertyName = new StringBuilder();
                for (i += 2; i < value.length() && value.charAt(i) != '}'; i++) {
                    propertyName.append(value.charAt(i));
                }
                if (propertyManager != null && !isAttribute) {
                    propertyManager.readAccess(propertyName.toString());
                }
                String name = toCamelCase(propertyName.toString());
                if (i + 1 < value.length() && Character.isJavaIdentifierPart(value.charAt(i + 1))) {
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

    public static String escapeTemplates(String value) {
        StringBuilder result = new StringBuilder(value);
        for (int i = result.indexOf("$"); i != -1; i = result.indexOf("$", i)) {
            if (result.length() > i + 1 &&
                    (Character.isLetter(result.charAt(i + 1)) || result.charAt(i + 1) == '_')) {
                result.replace(i, i + 1, "${\"$\"}");
                i += 6;
            } else {
                i++;
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
