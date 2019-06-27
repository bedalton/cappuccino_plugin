package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.inference.INFERRED_ANY_TYPE
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.PropertiesMap
import cappuccino.ide.intellij.plugin.inference.inferExpressionType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListFunctionType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListClass
import cappuccino.ide.intellij.plugin.jstypedef.contributor.plus
import cappuccino.ide.intellij.plugin.psi.ObjJExpr
import cappuccino.ide.intellij.plugin.psi.ObjJObjectLiteral
import cappuccino.ide.intellij.plugin.psi.ObjJPropertyAssignment
import cappuccino.ide.intellij.plugin.psi.ObjJPropertyName
import com.intellij.openapi.util.Key

object ObjJObjectPsiUtils {

    fun toJsObjectTypeSimple(element: ObjJObjectLiteral): JsTypeListClass {
        val storedDefinition: JsTypeListClass? = element.getUserData(OBJECT_LITERAL_EXPANDED_JS_OBJECT_KEY)
                ?: element.stub?.objectWithoutInference
        if (storedDefinition != null)
            return storedDefinition

        val out = mutableSetOf<JsTypeDefNamedProperty>()
        element.propertyAssignmentList.forEach {
            val name = it.propertyName.stringLiteral?.stringValue ?: it.propertyName.text
            val subObject = toJsObjectTypeSimple(it.expr)
            out.add(JsTypeDefNamedProperty(
                    name = name,
                    types = subObject ?: INFERRED_ANY_TYPE,
                    readonly = false,
                    static = false)
            )
        }
        return JsTypeListClass(
                allProperties = out,
                allFunctions = setOf()
        )
    }


    private fun toJsObjectTypeSimple(expr: ObjJExpr?): InferenceResult? {
        if (expr == null || expr.rightExprList.isNotEmpty())
            return null
        val element = expr.leftExpr?.objectLiteral ?: return null
        val properties = toJsObjectTypeSimple(element)
        if (properties.allProperties.isNullOrEmpty())
            return null
        return InferenceResult(
                types = setOf(
                        JsTypeListType.JsTypeListBasicType("Object"),
                        properties
                )
        )
    }

    fun toJsObjectType(element: ObjJObjectLiteral, tag: Long): JsTypeListClass {
        val outProperties = mutableSetOf<JsTypeDefNamedProperty>()
        val outFunctions = mutableSetOf<JsTypeListFunctionType>()
        element.propertyAssignmentList.forEach {
            val name = it.propertyName.stringLiteral?.stringValue ?: it.propertyName.text
            val expr = it.expr
            val type = inferExpressionType(expr, tag) ?: INFERRED_ANY_TYPE
            if (type.functionTypes.isNotEmpty()) {
                outFunctions.addAll(type.functionTypes)
            } else {
                outProperties.add(
                        JsTypeDefNamedProperty(
                                name = name,
                                static = false,
                                readonly = false,
                                default = null,
                                comment = null,
                                types = type
                        )
                )
            }
        }
        val expanded = JsTypeListClass(allProperties = outProperties, allFunctions = outFunctions)
        element.putUserData(OBJECT_LITERAL_EXPANDED_JS_OBJECT_KEY, expanded)
        return expanded
    }

    fun getNamespacedName(propertyName: ObjJPropertyName): String {
        var out = propertyName.key
        var parent = propertyName.parent.parent.parent.parent.parent as? ObjJPropertyAssignment
        while (parent != null) {
            out = "${parent.key}.$out"
            parent = parent.parent.parent.parent.parent as? ObjJPropertyAssignment
        }
        return out

    }

    fun getKey(propertyAssignment: ObjJPropertyAssignment): String {
        return propertyAssignment.propertyName.key
    }

    fun getKey(propertyName: ObjJPropertyName): String {
        return propertyName.stringLiteral?.stringValue ?: propertyName.text
    }

}

operator fun JsTypeListClass.plus(other: JsTypeListClass): JsTypeListClass {
    val properties = mutableSetOf<JsTypeDefNamedProperty>()
    val instanceProperties = mutableListOf<String>()
    val staticProperties = mutableListOf<String>()
    this.instanceProperties.forEach { thisProperty ->
        val propertyName = thisProperty.name
        val otherWithName = other.getInstanceProperty(propertyName)
        properties.add(thisProperty + otherWithName)
        instanceProperties.add(propertyName)
    }
    this.staticProperties.forEach { thisProperty ->
        val propertyName = thisProperty.name
        val otherWithName = other.getStaticProperty(propertyName)
        properties.add(thisProperty + otherWithName)
        staticProperties.add(propertyName)
    }
    val functions = this.allFunctions + other.allFunctions
    other.instanceProperties.filter { it.name !in instanceProperties }.forEach {
        properties.add(it)
    }
    other.staticProperties.filter { it.name !in staticProperties }.forEach {
        properties.add(it)
    }
    return JsTypeListClass(allProperties = properties, allFunctions = functions)
}

private val OBJECT_LITERAL_EXPANDED_JS_OBJECT_KEY = Key<JsTypeListClass>("objj.inference.JS_OBJECT")