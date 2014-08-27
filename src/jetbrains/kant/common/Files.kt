package jetbrains.kant.gtcommon

import java.io.File

public fun File.cleanDirectory() {
    if (!exists()) {
        return
    }
    val files = listFiles()
    for (file in files!!) {
        if (file.isDirectory()) {
            file.cleanDirectory()
        }
        file.delete()
    }
}

public fun File.deleteRecursively() {
    cleanDirectory()
    delete()
}
