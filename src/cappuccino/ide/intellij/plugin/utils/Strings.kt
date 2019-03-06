package cappuccino.ide.intellij.plugin.utils

import java.lang.StringBuilder

object Strings {

    @JvmOverloads
    fun notNull(string: String?, defaultVal: String = ""): String {
        return string ?: defaultVal
    }

    fun upperCaseFirstLetter(string: String?): String? {
        if (string == null) {
            return null
        }
        return if (string.length < 2) {
            string.toUpperCase()
        } else string.substring(0, 1).toUpperCase() + string.substring(1)
    }

    fun substringFromEnd(string:String, start:Int, fromEnd:Int) : String =
            string.substring(start, string.length - fromEnd)

}

fun String?.notNullOrDefault(defaultValue: String) : String =
        this ?: defaultValue

fun String.upperCaseFirstLetter() : String =
        Strings.upperCaseFirstLetter(this) ?: this

fun String.substringFromEnd(start:Int, subtractFromEnd:Int) : String =
        Strings.substringFromEnd(this, start, subtractFromEnd)

fun String.repeat(times:Int) : String {
    val stringBuilder = StringBuilder()
    for(i in 1..times) {
        stringBuilder.append(this)
    }
    return stringBuilder.toString()
}