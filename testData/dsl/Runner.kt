import jetbrains.kant.dsl.*
import jetbrains.kant.main
import jetbrains.kant.test.TEST_PLAYGROUND_BIN_DIR

var result = ""

object project : DSLProject() {
    {
        result = "OK"
    }
}

fun box(): String {
    main(array("-cp", TEST_PLAYGROUND_BIN_DIR, "project"))
    return result
}
