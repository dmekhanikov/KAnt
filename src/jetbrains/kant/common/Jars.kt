package jetbrains.kant.gtcommon

import java.io.InputStream
import java.io.OutputStream
import java.io.BufferedInputStream
import java.io.File
import java.util.jar.JarOutputStream
import java.util.jar.JarEntry
import java.io.FileInputStream
import java.util.jar.Manifest
import java.util.jar.Attributes
import java.io.FileOutputStream

public fun copy(inputStream: InputStream, outputStream: OutputStream) {
    val BUFFER_SIZE = 1024
    val buffer = ByteArray(BUFFER_SIZE)
    val bufferedInputStream = BufferedInputStream(inputStream)
    var len: Int
    while (true) {
        len = bufferedInputStream.read(buffer)
        if (len == -1) {
            break
        }
        outputStream.write(buffer, 0, len)
    }
    bufferedInputStream.close()
}

private fun copyFilesRecursively(dir: File, jarOutputStream: JarOutputStream, prefLen: Int) {
    for (file in dir.listFiles()!!) {
        if (file.isDirectory()) {
            copyFilesRecursively(file, jarOutputStream, prefLen)
        } else {
            val fileName = file.getCanonicalPath().substring(prefLen).replace('\\', '/')
            val jarEntry = JarEntry(fileName)
            jarEntry.setTime(file.lastModified())
            jarOutputStream.putNextEntry(jarEntry)
            val fileInputStream = FileInputStream(file)
            copy(fileInputStream, jarOutputStream)
            jarOutputStream.closeEntry()
        }
    }
}

public fun createJar(jarFile: String, srcDir: String) {
    val manifest = Manifest()
    manifest.getMainAttributes()!!.put(Attributes.Name.MANIFEST_VERSION, "1.0")
    File(jarFile).getParentFile()!!.mkdirs()
    val jarOutputStream = JarOutputStream(FileOutputStream(jarFile), manifest)
    val srcDirFile = File(srcDir).getCanonicalFile()
    val srcDirCanonical = srcDirFile.getCanonicalPath()
    val prefLen = srcDirCanonical.length + if (srcDirCanonical.endsWith('/')) { 0 } else { 1 }
    copyFilesRecursively(srcDirFile, jarOutputStream, prefLen)
    jarOutputStream.close()
}
