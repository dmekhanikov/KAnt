import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringProcessor {
    public static String toCamelCase(String name) {
        StringBuilder stringBuilder = new StringBuilder(name.toLowerCase());
        String separators = ".-_";
        for (int i = 0; i < separators.length(); i++) {
            String separator = String.valueOf(separators.charAt(i));
            for (int j = stringBuilder.indexOf(separator); j != -1; j = stringBuilder.indexOf(separator, j)) {
                stringBuilder.deleteCharAt(j);
                stringBuilder.setCharAt(j, Character.toUpperCase(stringBuilder.charAt(j)));
            }
        }
        return stringBuilder.toString();
    }

    public static String prepareValue(String value) {
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
        for (int i = 0; i < value.length(); i++) {
            if (i + 1 < value.length() &&
                    (value.charAt(i) == '$' || value.charAt(i) == '@') &&
                    value.charAt(i + 1) == '{') {
                StringBuilder propertyName = new StringBuilder();
                for (i += 2; i < value.length() && value.charAt(i) != '}'; i++) {
                    propertyName.append(value.charAt(i));
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
        result.append("\"");
        String pattern = "\"\\$(\\w*)\"";
        Matcher matcher = Pattern.compile(pattern).matcher(result);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return result.toString();
    }
}
