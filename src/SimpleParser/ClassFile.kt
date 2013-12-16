package SimpleGenerator

import java.io.InputStream
import java.util.HashSet

import org.objectweb.asm.commons.EmptyVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.ClassReader

open class ClassFile(inputStream : InputStream) {

    protected var methods : HashSet<Method> = HashSet<Method>()

    private val classReader = ClassReader(inputStream);
    {
        classReader.accept(ClassFileMethodVisiter(),
                ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        inputStream.close()
    }

    public fun methods(): HashSet<Method> {
        return HashSet<Method>(methods)
    }

    private inner class ClassFileMethodVisiter(): EmptyVisitor() {

        override fun visitMethod(access : Int,
                                 name : String?,
                                 signature : String?,
                                 desc : String?,
                                 exceptions : Array<out String>?): MethodVisitor? {
            methods.add(Method(name!!, signature!!))
            return this
        }
    }
}