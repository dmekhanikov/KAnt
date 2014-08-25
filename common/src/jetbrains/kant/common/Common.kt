package jetbrains.kant.common

import java.util.ArrayList
import java.net.URL
import java.net.MalformedURLException
import java.net.URLClassLoader
import java.io.File
import java.util.HashMap

public fun createClassLoader(jars: Array<String>, parent: ClassLoader?): ClassLoader {
    val path = ArrayList<URL>()
    for (jar in jars) {
        try {
            if (jar.endsWith(".jar")) {
                path.add(URL("jar:file:" + jar + "!/"))
            } else {
                path.add(URL("file:" + jar))
            }
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
    }
    return URLClassLoader(path.toArray(array(path[0])), parent)
}

public fun createClassLoader(classpath: String, parent: ClassLoader?): ClassLoader {
    return createClassLoader(classpath.split(File.pathSeparator), parent)
}

public fun <K, V>List<V>.valuesToMap(key: (V) -> K): Map<K, V> {
    val result = HashMap<K, V>()
    for (value in this) {
        result[key(value)] = value
    }
    return result
}
