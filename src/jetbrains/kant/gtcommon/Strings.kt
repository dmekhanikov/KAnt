package jetbrains.kant.gtcommon

import jetbrains.kant.gtcommon.constants.keywords
import java.util.ArrayList
import java.util.regex.Pattern

fun escapeKeywords(string: String): String {
    return if (keywords.contains(string)) {
        "`$string`"
    } else {
        string
    }
}

fun toCamelCase(name: String): String {
    val stringBuilder = StringBuilder(name)
    val separators = ".-_ "
    for (c in separators) {
        val separator = c.toString()
        var j = stringBuilder.indexOf(separator)
        while (j != -1) {
            stringBuilder.deleteCharAt(j)
            if (stringBuilder.length > j) {
                stringBuilder.setCharAt(j, Character.toUpperCase(stringBuilder[j]))
            }
            j = stringBuilder.indexOf(separator, j)
        }
    }
    return escapeKeywords(stringBuilder.toString())
}

fun getClassByPackage(pkgName: String): String {
    return if (pkgName == "") {
        "_DefaultPackage"
    } else {
        "$pkgName.${pkgName.split('.').last().capitalize()}Package"
    }
}

fun explodeTypeName(name: String): List<String> {
    val result = ArrayList<String>()
    val pattern = Pattern.compile("([^<>]*)<(.*)>")
    var remaining = name
    var matcher = pattern.matcher(remaining)
    while (matcher.matches()) {
        result.add(matcher.group(1)!!)
        remaining = matcher.group(2)!!
        matcher = pattern.matcher(remaining)
    }
    result.add(remaining)
    return result.toList()
}
