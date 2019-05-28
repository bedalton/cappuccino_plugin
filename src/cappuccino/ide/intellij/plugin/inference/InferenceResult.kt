package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.contributor.GlobalJSClass
import cappuccino.ide.intellij.plugin.utils.orFalse


data class InferenceResult (
        val isNumeric:Boolean = false,
        val isBoolean:Boolean = false,
        val isString:Boolean = false,
        val isDictionary:Boolean = false,
        val isSelector:Boolean = false,
        val isRegex:Boolean = false,
        val jsObjectKeys:List<String>? = null,
        val functionTypes:List<JsFunctionType>? = null,
        val classes:List<GlobalJSClass> = emptyList()
) {
    val isJsObject:Boolean by lazy {
        jsObjectKeys?.isNotEmpty().orFalse()
    }
}

data class JsFunctionType (
        val parameters:List<String>,
        val returnType:List<String>
)