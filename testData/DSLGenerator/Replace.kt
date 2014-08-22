import jetbrains.kant.dsl.*
import jetbrains.kant.dsl.taskdefs.*

val file by StringProperty()

val replaceProject = object : DSLProject() {
    {
        echo(message = "cat", file = file)
        replace(file = file) {
            replaceToken {
                text { "cat" }
            }
            replaceValue {
                text { "wombat" }
            }
        }
    }
}
