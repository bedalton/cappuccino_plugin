package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJQualifiedReferenceComponent
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ReferencesSearch

internal fun inferQualifiedReferenceType(qualifiedReference:ObjJQualifiedReference, levels: Int) : InferenceResult? {
    val parts= qualifiedReference.qualifiedNameParts
    if (parts.isEmpty())
        return null
    if (parts.size == 0) {
        return null
    }
    var parentTypes:List<String>
    for(i in 0 until parts.size) {
        val part = parts[i]
        if (i == 0) {

        }
    }
}

internal fun getPartTypes(part:ObjJQualifiedReferenceComponent) : List<String> {
    
}

internal fun inferVariableNameType(variableName:ObjJVariableName, levels:Int) : InferenceResult? {
    if (levels - 1 < 0) {
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

