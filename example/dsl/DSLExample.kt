import java.io.File
import ru.ifmo.rain.mekhanikov.ant2kotlin.dsl.project

fun main(args : Array<String>) =
    project {
        default = "mkdir"
        target("mkdir") {
            mkdir {
                dir = File("/home/user/temp")
            }
        }
    }
