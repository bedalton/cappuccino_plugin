package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.inference.INFERRED_ANY_TYPE
import cappuccino.ide.intellij.plugin.psi.ObjJExpr
import cappuccino.ide.intellij.plugin.psi.ObjJObjectLiteral
import com.intellij.openapi.util.Key

object ObjJObjectPsiUtils {

    fun toJsObjectTypeSimple(element:ObjJObjectLiteral) : JsObjectType {
        val storedDefinition = element.getUserData(OBJECT_LITERAL_EXPANDED_JS_OBJECT_KEY) ?: element.stub?.objectWithoutInference
        if (storedDefinition != null)
            return storedDefinition

        val out = mutableMapOf<String, InferenceResult>()
        element.propertyAssignmentList.forEach {
            val name = it.propertyName.stringLiteral?.stringValue ?: it.propertyName.text
            val subObject = toJsObjectTypeSimple(it.expr)
            out[name] = subObject ?: INFERRED_ANY_TYPE
        }
        return JsObjectType(out)
    }


    private fun toJsObjectTypeSimple(expr:ObjJExpr?) : InferenceResult? {
        if (expr == null || expr.rightExprList.isNotEmpty())
            return null
        val element = expr.leftExpr?.objectLiteral ?: return null
        val properties = toJsObjectTypeSimple(element).properties
        if (properties.isEmpty())
            return null
        return InferenceResult(
                classes = setOf("object"),
                jsObjectKeys = properties
        )
    }

    fun toJsObjectType(element:ObjJObjectLiteral, tag:Long) : JsObjectType {
        val out = mutableMapOf<String, InferenceResult>()
        element.propertyAssignmentList.forEach {
            val name = it.propertyName.stringLiteral?.stringValue ?: it.propertyName.text
            val expr = it.expr
            val type = inferExpressionType(expr, tag) ?: INFERRED_ANY_TYPE
            out[name] = type
        }
        val expanded = JsObjectType(out)
        element.putUserData(OBJECT_LITERAL_EXPANDED_JS_OBJECT_KEY, expanded)
        return expanded
    }


}

data class JsObjectType (val properties:PropertiesMap) {
    operator fun get(key:String) : InferenceResult?
            = properties[key]

    fun containsKey(key:String)
            = properties.containsKey(key)
}

operator fun JsObjectType.plus(other: JsObjectType) : JsObjectType {
    val properties = mutableMapOf<String, InferenceResult>()
    this.properties.forEach { (key, value) ->
        val otherWithName = other[key]
        properties[key] = if (otherWithName != null)
            value + otherWithName
        else
            value
    }
    val currentKeys = properties.keys
    other.properties.filter { it.key !in currentKeys }.forEach { (key, value) ->
        properties[key] = value
    }
    return JsObjectType(properties = properties)
}

private val OBJECT_LITERAL_EXPANDED_JS_OBJECT_KEY = Key<JsObjectType>("objj.inference.JS_OBJECT")