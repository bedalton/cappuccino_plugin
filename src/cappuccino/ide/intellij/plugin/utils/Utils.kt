package cappuccino.ide.intellij.plugin.utils

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import java.util.*


private const val PLUGIN_ID = "cappuccino.intellij.plugin"
val PLUGIN by lazy {
    PluginManagerCore.getPlugin(PluginId.getId(PLUGIN_ID))
}
val PLUGIN_VERSION:String by lazy {
    PLUGIN?.version ?: ""
}

val now:Long get() = Date().time

fun Regex.doesNotMatch(test:String) : Boolean {
    return !matches(test)
}

fun String?.ifEmptyNull() : String? {
    return if (isNotNullOrBlank()) this else null
}

fun <T> T?.ifEquals(value:T?, generate:(T?) -> T?) : T? {
    if (this == value)
        return generate(this)
    return this
}

fun <T> T.nullIfEquals(value:T) : T? {
    if (this == value)
        return null
    return this
}