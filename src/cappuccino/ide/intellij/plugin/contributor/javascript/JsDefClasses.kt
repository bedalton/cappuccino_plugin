package cappuccino.ide.intellij.plugin.contributor.javascript

data class JsParam(val paramName:String,
                   val param:String?,
                   val params:List<String>?,
                   val readonly:Boolean = false)

class JsFunctionDef(val name:String, val params:List<JsParam>?)

data class JsClass(val className:String,
                   val params:List<JsParam> = listOf(),
                   val functions:List<JsFunctionDef> = listOf())