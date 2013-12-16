import junit.framework.TestCase
import kotlin.test.assertEquals
import java.io.FileInputStream
import java.io.File
import SimpleGenerator.ClassFile

class ClassFileTests : TestCase() {
    public fun test01() {
        val classFile = ClassFile(FileInputStream(File("Tests/res/ClassFileTests/Javac.class")))
        assertEquals(87, classFile.methods().size())
    }

    public fun test02() {
        val classFile = ClassFile(FileInputStream(File("Tests/res/ClassFileTests/Target.class")))
        assertEquals(29, classFile.methods().size())
    }

    public fun test03() {
        val classFile = ClassFile(FileInputStream(File("Tests/res/ClassFileTests/Task.class")))
        assertEquals(29, classFile.methods().size())
    }
}