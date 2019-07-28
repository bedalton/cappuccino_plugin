package cappuccino.ide.intellij.plugin.utils

import java.util.*

const val PLUGIN_VERSION:String = "0.4.0"



val now:Long get() = Date().time

fun Regex.doesNotMatch(test:String) : Boolean {
    return !matches(test)
}

fun String?.ifEmptyNull() : String? {
    return if (isNotNullOrBlank()) this else null
}