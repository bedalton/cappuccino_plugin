package cappuccino.ide.intellij.plugin.utils



fun Boolean?.orDefault(defaultValue:Boolean) : Boolean {
    return this ?: defaultValue
}

fun Boolean?.orFalse() : Boolean {
    return this ?: false
}

fun Boolean?.orTrue() : Boolean {
    return this ?: true
}