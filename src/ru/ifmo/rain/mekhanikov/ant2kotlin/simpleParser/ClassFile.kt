package ru.ifmo.rain.mekhanikov.ant2kotlin.simpleParser

import java.io.InputStream
import java.util.HashSet

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.commons.Method
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

open class ClassFile(inputStream : InputStream) {

    protected val methods : HashSet<Method> = HashSet<Method>()

    private val classReader = ClassReader(inputStream);
    {
        classReader.accept(ClassFileMethodVisitor(),
                ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        inputStream.close()
    }

    public fun methods(): HashSet<Method> {
        return HashSet<Method>(methods)
    }

    private inner class ClassFileMethodVisitor(): ClassVisitor(Opcodes.ASM4) {
        override public fun visitMethod(access : Int,
                                        name : String?,
                                        desc : String?,
                                        signature : String?,
                                        exceptions : Array<out String>?) : MethodVisitor? {
            methods.add(Method(name, desc))
            return null
        }

    }
}