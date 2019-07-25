package cappuccino.ide.intellij.plugin.utils

import java.util.*

val now:Long get() = Date().time

fun Regex.doesNotMatch(test:String) : Boolean {
    return !matches(test)
}