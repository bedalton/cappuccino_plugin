package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.contributor.getJsClassObject
import cappuccino.ide.intellij.plugin.contributor.objJClassAsJsClass
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJQualifiedReferenceComponent
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassTypeName
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import cappuccino.ide.intellij.plugin.utils.substringFromEnd
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ReferencesSearch
import org.intellij.lang.annotations.RegExp

internal fun inferQualifiedReferenceType(qualifiedReference:ObjJQualifiedReference, level: Int) : InferenceResult? {
    val parts= qualifiedReference.qualifiedNameParts
    if (parts.isEmpty())
        return null
    if (parts.size == 0) {
        return null
    }
    var parentTypes:InferenceResult? = null
    val next:ObjJQualifiedReferenceComponent
    for(i in 0 until parts.size) {
        val part = parts[i]
        if (i == 0) {
            parentTypes = getPartTypes(part, parentTypes, level - 1)
        }
    }
}

val splitJsClassRegExp = "\\s*\\|\\s*".toRegex()

internal fun getPartTypes(part:ObjJQualifiedReferenceComponent, parentTypes:InferenceResult?, level:Int) : InferenceResult? {
    return when (part) {
        is ObjJVariableName -> getVariableNameComponentTypes(part, parentTypes, level)
        is ObjJFunction ->
        else -> return InferenceResult(
                classes = listOf(ObjJClassType.UNDEF_CLASS_NAME)
        )
    }
}

private val booleanTypes = listOf("bool", "boolean")
private val stringTypes = listOf("string", "cpstring")
private val numberTypes = listOf("number", "int", "integer", "float", "long", "double")

internal fun getVariableNameComponentTypes(variableName:ObjJVariableName, parentTypes:InferenceResult?, level:Int) : InferenceResult? {
    if (parentTypes == null) {
        return inferVariableNameType(variableName, level - 1)
    }
    val project = variableName.project
    val variableNameString = variableName.text
    val classNames = parentTypes.classes.flatMap { jsClass ->
        jsClass.properties.firstOrNull {
            it.name == variableNameString
        }?.type?.split(splitJsClassRegExp) ?: emptyList()
    }.toSet()
    val arrayClasses = classNames.filter { it.endsWith("[]")}.map { it.substringFromEnd(0, 2) }
    val classes = classNames.mapNotNull {
        getJsClassObject(project, it)
    }
    return InferenceResult(
            isString = classNames.any{ it.toLowerCase() in stringTypes},
            isBoolean = classNames.any{ it.toLowerCase() in booleanTypes},
            isNumeric = classNames.any{ it.toLowerCase() in numberTypes},
            arrayTypes = arrayClasses,
            classes = classes
    )
}

internal fun inferVariableNameType(variableName:ObjJVariableName, levels:Int) : InferenceResult? {
    if (levels < 0) {
        return null
    }
    if (variableName.indexInQualifiedReference != 0)
        return null
    val resolved = variableName.reference.resolve()
    if (resolved is ObjJVariableName) {
        findReferencingUsageTypes(resolved)
    } else {

    }
}

private fun findReferencingUsageTypes(referencedVariable:ObjJVariableName) : InferenceResult {
    val usages = ReferencesSearch.search(referencedVariable)
                    .findAll()
    val isNumericFromAssignmentExpression = usages.mapNotNull {
        it.element.getParentOfType(ObjJVariableAssignmentLogical::class.java)?.assignmentExprPrime
    }.isNotEmpty()
    val assignments = usages.mapNotNull{ getAssignedExpressions(it.element)}
    return getInferredTypeFromExpressionArray(assignments)
}

private fun getAssignedExpressions(element:PsiElement?) : ObjJExpr? {
    return if (element == null || element !is ObjJVariableName)
        null
    else if (element.parent is ObjJGlobalVariableDeclaration)
        (element.parent as ObjJGlobalVariableDeclaration).expr
    else if (element.parent !is ObjJQualifiedReference)
        null
    else if (element.parent.parent is ObjJVariableDeclaration)
        (element.parent.parent as ObjJVariableDeclaration).expr
    else
        null
}

internal fun findPreviousUsageTypes(variableName: ObjJVariableName, levels:Int = 3) : InferenceResult {

}

