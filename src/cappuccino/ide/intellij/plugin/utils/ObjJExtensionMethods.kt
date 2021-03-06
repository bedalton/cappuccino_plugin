package cappuccino.ide.intellij.plugin.utils

import com.intellij.openapi.util.TextRange
import com.intellij.psi.stubs.StubInputStream


fun Boolean?.orDefault(defaultValue:Boolean) : Boolean {
    return this ?: defaultValue
}

fun Boolean?.orFalse() : Boolean {
    return this ?: false
}

fun Boolean?.orTrue() : Boolean {
    return this ?: true
}

fun <T> T?.orElse(defaultValue: T) : T {
    return this ?: defaultValue
}

fun <T> T?.or(defaultValue: T) : T {
    return this ?: defaultValue
}


fun Int?.add(other:Int?) : Int? {
    if (this == null || other == null) {
        return null
    }
    return this + other
}

fun Long?.add(other:Long?) : Long? {
    if (this == null || other == null) {
        return null
    }
    return this + other
}

fun Float?.add(other:Float?) : Float? {
    if (this == null || other == null) {
        return null
    }
    return this + other
}


fun Double?.add(other:Double?) : Double? {
    if (this == null || other == null) {
        return null
    }
    return this + other
}


/**
 * Returns `true` if this nullable char sequence is either `null` or empty or consists solely of whitespace characters.
 */
fun CharSequence?.isNotNullOrBlank(): Boolean {
    return this != null && this.isNotBlank()
}

/**
 * Returns `true` if this nullable char sequence is either `null` or empty or consists solely of whitespace characters.
 */
fun CharSequence?.isNotNullOrEmpty(): Boolean {
    return this != null && this.isNotEmpty()
}

fun TextRange.isValid(): Boolean {
    return startOffset in 0..endOffset
}

fun TextRange.isInvalid(): Boolean {
    return !isValid()
}
