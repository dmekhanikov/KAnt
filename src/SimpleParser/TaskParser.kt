package SimpleGenerator

import java.io.InputStream
import java.util.HashSet

class TaskParser(inputStream : InputStream) : ClassFile(inputStream) {

    public fun booleanFields(): HashSet<String> {
        var ans = HashSet<String>()
        for (method in methods) {
            if (method.name.startsWith("set") && method.signature.equals("(Z)V")) {
                ans.add(cutName(method.name))
            }
        }
        return ans
    }

    public fun stringFields(): HashSet<String> {
        var ans = HashSet<String>()
        for (method in methods) {
            if (method.name.startsWith("set") && method.signature.equals("(Ljava/lang/String;)V")) {
                ans.add(cutName(method.name))
            }
        }
        return ans
    }

    private fun cutName(name : String): String {
        return Character.toLowerCase(name.charAt(3)).toString() + name.substring(4)
    }
}