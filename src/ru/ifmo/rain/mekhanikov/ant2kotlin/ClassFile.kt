package ru.ifmo.rain.mekhanikov.ant2kotlin

import java.io.InputStream

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.commons.Method
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import java.util.ArrayList
import java.util.Arrays

open class ClassFile(inputStream : InputStream) {

    protected var name : String = ""
    protected val publicMethods: ArrayList<Method> = ArrayList<Method>()
    protected val interfaces : ArrayList<String> = ArrayList<String>()

    private val classReader = ClassReader(inputStream);
    {
        classReader.accept(ClassFileMethodVisitor(),
                ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES)
        inputStream.close()
    }

    public fun interfaces(): List<String> {
        return interfaces
    }

    public fun publicMethods(): List<Method> {
        return publicMethods
    }

    private inner class ClassFileMethodVisitor(): ClassVisitor(Opcodes.ASM4) {
        public override fun visit(version : Int,
                                  access : Int,
                                  name : String?,
                                  signature : String?,
                                  superName : String?,
                                  interfaces : Array<out String>?) {
            this@ClassFile.name = name!!
            for (i in interfaces!!) {
                this@ClassFile.interfaces.add(i)
            }
        }

        public override fun visitMethod(access : Int,
                                        name : String?,
                                        desc : String?,
                                        signature : String?,
                                        exceptions : Array<out String>?) : MethodVisitor? {
            if (access and Opcodes.ACC_PUBLIC != 0) {
                publicMethods.add(Method(name, desc))
            }
            return null
        }
    }
}
