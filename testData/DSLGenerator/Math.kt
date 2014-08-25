import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.*
import jetbrains.kant.dsl.other.net.sf.antcontrib.math.*

val destFile by StringProperty()
val antContribJarFile by StringProperty()
val fact by DoubleProperty()

object mathProject : DSLProject() {
    {
        taskdef(resource = "net/sf/antcontrib/antcontrib.properties",
                classpath = antContribJarFile)
        default = ::testMath
    }

    val testMath = target {
        math(result = "fact") {
            op(op = "*") {
                num(value = "1")
                num(value = "2")
                num(value = "3")
                num(value = "4")
                num(value = "5")
            }
        }
        echo(message = "$fact", file = destFile)
    }
}
