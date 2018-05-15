package cappuccino.ide.intellij.plugin.psi.utils

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import cappuccino.ide.intellij.plugin.exceptions.CannotDetermineException
import cappuccino.ide.intellij.plugin.exceptions.IndexNotReadyInterruptingException
import cappuccino.ide.intellij.plugin.indices.ObjJInstanceVariablesByClassIndex
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.ARRAY
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.BOOL
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.CPOBJECT
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.DOUBLE
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.FLOAT
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.ID
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.INT
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.JS_FUNCTION
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.LONG
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.NIL
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.SEL
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.STRING
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.UNDETERMINED
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType.Companion.isPrimitive
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import java.util.*

typealias SubExpressionTest = (ObjJExpr?) -> Boolean
typealias RightExpressionTest = (ObjJRightExpr) -> Boolean;

@Throws(IndexNotReadyInterruptingException::class)
@JvmOverloads

fun ObjJExpr?.isReturnTypeInstanceOf(classType: String?, project: Project, defaultValueIfNull: Boolean = false): Boolean {
    if (this == null) {
        return false
    }
    if (classType == null) {
        return defaultValueIfNull
    }
    val returnType: String?
    try {
        returnType = getReturnType(expr, true)
    } catch (e: MixedReturnTypeException) {
        return e.returnTypesList.contains(classType)
    }

    if (returnType == null) {
        return defaultValueIfNull
    }
    if (returnType == UNDETERMINED || classType == returnType) {
        return true
    }

    if (classType == ID) {
        return !isPrimitive(classType)
    }
    if (classType == LONG) {
        if (returnType == INT) {
            return true
        }
    }
    try {
        return ObjJClassType.isSubclassOrSelf(classType, returnType, project)
    } catch (ignored: CannotDetermineException) {
        return defaultValueIfNull
    }

}

fun ObjJExpr?.getReturnTypes(): ExpressionReturnTypeResults? {
    return if (this == null) {
        null
    } else getReturnTypes(ExpressionReturnTypeResults(project), this)
}

fun getReturnTypes(results: ExpressionReturnTypeResults, expr: ObjJExpr?): ExpressionReturnTypeResults? {
    if (expr == null) {
        return null
    }
    if (testAllSubExpressions(expr, { isReturnTypeString(it) })) {
        results.tick(STRING)
    } else if (testAllSubExpressions(expr, { isReturnTypeInteger(it) })) {
        results.tick(INT)
    } else if (testAllSubExpressions(expr, { isReturnTypeFloat(it) })) {
        results.tick(FLOAT)
    } else if (testAllSubExpressions(expr, { isReturnTypeDouble(it) })) {
        results.tick(DOUBLE)
    } else if (testAllSubExpressions(expr, { isReturnTypeBOOL(it) })) {
        results.tick(BOOL)
    }
    for (varType in expr.getVariableNameType()) {
        results.tick(varType)
    }
    getReturnTypesFromMethodCall(results, expr)
    return results
}

@Throws(MixedReturnTypeException::class)
fun getReturnType(expr: ObjJExpr?, follow: Boolean): String? {
    if (expr == null) {
        return null
    }
    if (testAllSubExpressions(expr, { isReturnTypeString(it) })) {
        return STRING
    }
    if (testAllSubExpressions(expr, { isReturnTypeFloat(it) })) {
        return FLOAT
    }
    if (testAllSubExpressions(expr, { isReturnTypeDouble(it) })) {
        return DOUBLE
    }
    if (testAllSubExpressions(expr, { isReturnTypeInteger(it) })) {
        return INT
    }
    if (testAllSubExpressions(expr, { isReturnTypeBOOL(it) })) {
        return BOOL
    }
    var returnType = getSelfOrSuper(expr)
    if (returnType != null) {
        return returnType
    }
    returnType = getReturnTypeFromMethodCall(expr, follow)
    if (returnType != null) {
        return returnType
    }
    var returnTypes = expr.getVariableNameType()
    if (returnTypes.size == 1) {
        return returnTypes[0]
    } else if (!returnTypes.isEmpty()) {
        try {
            returnTypes = ObjJInheritanceUtil.reduceToDeepestInheritance(returnTypes, expr.project)
            if (returnTypes.size == 1) {
                return returnTypes[0]
            }
            throw MixedReturnTypeException(returnTypes)
        } catch (e: IndexNotReadyInterruptingException) {
            e.printStackTrace()
        }

    }
    return returnType ?: UNDETERMINED
}

private fun getReturnTypesFromMethodCall(results: ExpressionReturnTypeResults, expr: ObjJExpr?) {
    if (expr == null) {
        return
    }
    var returnType: String?
    for (exprElementInLoop in getAllSubExpressions(expr, true)) {
        val methodCall: ObjJMethodCall = exprElementInLoop.leftExpr?.methodCall ?: continue;
        try {
            returnType = getReturnTypeFromMethodCall(methodCall, true, null)
            if (returnType == null) {
                continue
            }
            results.tick(returnType)
        } catch (ignored: IndexNotReadyInterruptingException) {
        }
    }
}

private fun getReturnTypeFromMethodCall(expr: ObjJExpr?, follow: Boolean): String? {
    if (expr == null) {
        return null
    }
    val returnTypes = ArrayList<String>()
    var returnType: String? = null
    for (exprElementInLoop in getAllSubExpressions(expr, true)) {
        val methodCall: ObjJMethodCall = exprElementInLoop?.leftExpr?.methodCall ?: continue
        try {
            returnType = getReturnTypeFromMethodCall(methodCall, follow, null)
            if (returnType == null) {
                continue
            }
            if (!returnTypes.contains(returnType)) {
                returnTypes.add(returnType)
            }
        } catch (ignored: IndexNotReadyInterruptingException) {
        }
    }
    return if (returnTypes.isEmpty()) {
        null
    } else returnTypes[0]
}


private fun isReturnTypeString(expr: ObjJExpr?): Boolean {
    return if (expr == null) {
        false
    } else isLeftExpressionReturnTypeString(expr.leftExpr)
}

private fun isLeftExpressionReturnTypeString(leftExpr: ObjJLeftExpr?): Boolean {
    if (leftExpr == null) {
        return false
    }
    return if (leftExpr.primary == null) {
        false
    } else leftExpr.primary!!.stringLiteral != null || leftExpr.regularExpressionLiteral != null
}

@Throws(CannotDetermineException::class)
private fun isReturnTypeInteger(expr: ObjJExpr?): Boolean {
    if (expr == null) {
        return false
    }
    if (expr.bitNot != null) {
        return true
    }
    if (expr.leftExpr == null) {
        return false
    }
    val leftExpr = expr.leftExpr
    if (leftExpr!!.primary != null) {
        val primary = leftExpr.primary
        if (primary!!.integer != null) {
            return true
        }
    }
    if (leftExpr.plusPlus != null || leftExpr.minusMinus != null) {
        return true
    }
    for (rightExpr in expr.rightExprList) {
        if (isRightSideEvaluateToInteger(rightExpr)) {
            return true
        }
    }
    return false
}

@Throws(CannotDetermineException::class)
private fun isRightSideEvaluateToInteger(rightExpr: ObjJRightExpr): Boolean {
    if (rightExpr.boolAssignExprPrime != null) {
        return isReturnTypeInteger(rightExpr.boolAssignExprPrime!!.ifTrue) || isReturnTypeInteger(rightExpr.boolAssignExprPrime!!.ifFalse)
    }
    if (!rightExpr.arrayIndexSelectorList.isEmpty()) {
        throw CannotDetermineException()
    }
    if (rightExpr.assignmentExprPrime != null) {
        return isReturnTypeInteger(rightExpr.assignmentExprPrime!!.expr)
    }
    if (rightExpr.mathExprPrime != null) {
        if (rightExpr.mathExprPrime != null) {
            val mathExprPrime = rightExpr.mathExprPrime
            return mathExprPrime!!.bitAnd != null ||
                    mathExprPrime.bitNot != null ||
                    mathExprPrime.bitOr != null ||
                    mathExprPrime.bitXor != null ||
                    mathExprPrime.leftShiftArithmatic != null ||
                    mathExprPrime.rightShiftArithmatic != null ||
                    mathExprPrime.leftShiftLogical != null ||
                    mathExprPrime.rightShiftLogical != null ||
                    mathExprPrime.modulus != null ||
                    isReturnTypeInteger(mathExprPrime.expr)
        }
    }
    return if (rightExpr.boolAssignExprPrime != null) {
        isReturnTypeInteger(rightExpr.boolAssignExprPrime!!.ifFalse) || isReturnTypeInteger(rightExpr.boolAssignExprPrime!!.ifFalse)
    } else false
}


private fun isReturnTypeFloat(expr: ObjJExpr?): Boolean {
    return testAllSubExpressions(expr, { isReturnTypeFloatTest(it) })
}


@Throws(CannotDetermineException::class)
private fun isReturnTypeFloatTest(expr: ObjJExpr?): Boolean {
    if (expr == null) {
        return false
    }

    if (isReturnTypeInteger(expr)) {
        return true
    }

    if (expr.leftExpr == null) {
        return false
    }
    return if (expr.leftExpr!!.primary == null) {
        false
    } else expr.leftExpr!!.primary!!.decimalLiteral != null
}

@Throws(CannotDetermineException::class)
private fun isReturnTypeDouble(expr: ObjJExpr?): Boolean {
    return if (expr == null) {
        false
    } else isReturnTypeFloat(expr)
}

/**
 * Evaluates whether an expression evaluates to BOOL
 * @param expr expression element to test
 * @return **true** if expression evaluate to bool, **false** otherwise
 */
private fun isReturnTypeBOOL(expr: ObjJExpr?): Boolean {
    try {
        return testAllRightExpressions(expr, { isRightExpressionBool(it) })
    } catch (e: Exception) {
        return false
    }

}

/**
 * Evaluates whether a right side expression evaluates to BOOL
 * @param rightExpr right expression to test
 * @return **true** if right expression evaluate to bool, **false** otherwise
 */
private fun isRightExpressionBool(rightExpr: ObjJRightExpr?): Boolean {
    if (rightExpr == null) {
        return false
    }

    if (rightExpr.joinExprPrime != null || rightExpr.instanceOfExprPrime != null) {
        return true
    }
    if (rightExpr.boolAssignExprPrime != null) {
        val assignExprPrime = rightExpr.boolAssignExprPrime
        return isReturnTypeBOOL(assignExprPrime!!.ifTrue) || assignExprPrime.ifFalse != null && isReturnTypeBOOL(assignExprPrime.ifFalse)
    }
    if (rightExpr.booleanExprPrime != null) {
        return true
    }
    return if (rightExpr.assignmentExprPrime != null) {
        isReturnTypeBOOL(rightExpr.assignmentExprPrime!!.expr)
    } else false
}

private fun getAllSubExpressions(expr: ObjJExpr?, addSelf: Boolean = true): List<ObjJExpr> {
    if (expr == null) {
        return emptyList()
    }

    val leftExpr = expr.leftExpr
    val expressions = ArrayList<ObjJExpr>()
    if (addSelf) {
        expressions.add(expr)
    }
    if (expr.expr != null) {
        expressions.add(expr.expr!!)
    }
    if (leftExpr != null) {
        if (leftExpr.variableDeclaration != null) {
            expressions.add(leftExpr.variableDeclaration!!.expr)
        }
        if (leftExpr.variableAssignmentLogical != null) {
            val variableAssignmentLogical = leftExpr.variableAssignmentLogical
            expressions.add(variableAssignmentLogical!!.assignmentExprPrime.expr)
        }
    }
    addRightExpressionExpressions(expressions, expr.rightExprList)
    return expressions

}

private fun addRightExpressionExpressions(expressions: MutableList<ObjJExpr>, rightExpressions: List<ObjJRightExpr>) {
    for (rightExpr in rightExpressions) {
        if (rightExpr.assignmentExprPrime != null) {
            expressions.add(rightExpr.assignmentExprPrime!!.expr)
        }
        val boolAssignExprPrime = rightExpr.boolAssignExprPrime ?: continue
        expressions.add(boolAssignExprPrime.ifTrue)
        expressions.add(boolAssignExprPrime.ifFalse!!)
    }
}

private fun testAllSubExpressions(expr: ObjJExpr?, test:SubExpressionTest): Boolean {
    if (expr == null) {
        return false
    }
    try {
        if (test(expr)) {
            return true
        }
    } catch (ignored: CannotDetermineException) {
    }

    try {
        if (testLeftExpression(expr.leftExpr, test)) {
            return true
        }
    } catch (ignored: CannotDetermineException) {
    }

    for (rightExpr in expr.rightExprList) {
        try {
            if (testRightExpressionsExpressions(rightExpr, test)) {
                return true
            }
        } catch (ignored: Exception) {
        }

    }
    return false
}

@Throws(CannotDetermineException::class)
private fun testAllRightExpressions(expression: ObjJExpr?, test: RightExpressionTest): Boolean {
    if (expression == null) {
        return false
    }
    val expressions = getAllSubExpressions(expression)
    for (expressionInLoop in expressions) {
        for (rightExpression in expressionInLoop.rightExprList) {
            if (test(rightExpression)) {
                return true
            }
        }
    }
    return false
}

@Throws(CannotDetermineException::class)
private fun testLeftExpression(leftExpr: ObjJLeftExpr?, test: SubExpressionTest): Boolean {
    if (leftExpr == null) {
        return false
    }
    if (leftExpr.variableDeclaration != null) {
        if (test(leftExpr.variableDeclaration!!.expr)) {
            return true
        }
    }
    if (leftExpr.variableAssignmentLogical != null) {
        val variableAssignmentLogical = leftExpr.variableAssignmentLogical
        return test(variableAssignmentLogical!!.assignmentExprPrime.expr)
    }
    return false
}

@Throws(CannotDetermineException::class)
private fun testRightExpressionsExpressions(rightExpr: ObjJRightExpr?, test: SubExpressionTest): Boolean {
    if (rightExpr == null) {
        return false
    }
    if (rightExpr.assignmentExprPrime != null) {
        return test(rightExpr.assignmentExprPrime!!.expr)
    }
    return if (rightExpr.boolAssignExprPrime != null) {
        test(rightExpr.boolAssignExprPrime!!.ifTrue) || test(rightExpr.boolAssignExprPrime!!.ifFalse)
    } else false
}

@Throws(CannotDetermineException::class)
private fun getReturnTypeFromPrimary(primary: ObjJPrimary): String? {
    if (primary.booleanLiteral != null) {
        return BOOL
    }
    if (primary.decimalLiteral != null) {
        return FLOAT
    }
    if (primary.integer != null) {
        return INT
    }

    if (primary.nullLiterals != null) {
        return NIL
    }
    return if (primary.stringLiteral != null) {
        STRING
    } else null
}

private fun isParent(parent: PsiElement, childExpression: ObjJExpr): Boolean {
    return PsiTreeUtil.isAncestor(parent, childExpression, false)
}

@Throws(CannotDetermineException::class)
private fun getReturnTypeFromLeftExpression(leftExpr: ObjJLeftExpr?): String? {
    if (leftExpr == null) {
        throw CannotDetermineException()
    }
    if (leftExpr.arrayLiteral != null) {
        return ARRAY
    }
    if (leftExpr.minusMinus != null || leftExpr.plusPlus != null) {
        return INT
    }

    if (leftExpr.functionLiteral != null) {
        return JS_FUNCTION
    }

    if (leftExpr.selectorLiteral != null) {
        return SEL
    }

    if (leftExpr.objectLiteral != null) {
        return CPOBJECT
    }

    if (leftExpr.functionCall != null) {
        throw CannotDetermineException()
    }

    if (leftExpr.methodCall != null) {
        throw CannotDetermineException()
    }

    return null
}

@Throws(IndexNotReadyInterruptingException::class)
private fun getReturnTypeFromMethodCall(methodCall: ObjJMethodCall, follow: Boolean, defaultReturnType: String?): String? {
    val project = methodCall.project
    if (DumbService.isDumb(project)) {
        return null
    }
    if (methodCall.selectorString == "alloc" || methodCall.selectorString == "new") {
        return if (methodCall.callTarget.text == "self" || methodCall.callTarget.text == "super") {
            methodCall.containingClassName
        } else {
            methodCall.callTarget.text
        }
    }
    val methodHeaders = ObjJUnifiedMethodIndex.instance.get(methodCall.selectorString, project)
    if (methodHeaders.isEmpty()) {
        return null
    }
    var results: ExpressionReturnTypeResults? = null
    if (methodCall.callTarget.text == "self") {
        results = ExpressionReturnTypeResults(methodCall.project)
        val inheritance = ObjJInheritanceUtil.getInheritanceUpAndDown(methodCall.containingClassName, methodCall.project)
        results.tick(inheritance)
    } else if (methodCall.callTarget.text == "super") {
        if (methodCall.callTarget.text == "self") {
            results = ExpressionReturnTypeResults(methodCall.project)
            val containingClassName = methodCall.containingClassName
            val inheritance = ObjJInheritanceUtil.getAllInheritedClasses(containingClassName, methodCall.project)
            inheritance.remove(containingClassName)
            results.tick(inheritance)
        }
    } else if (methodCall.callTarget.expr != null) {
        results = methodCall.callTarget.expr.getReturnTypes()
    }
    val possibleClassNames = if (results != null) results.inheritanceUpAndDown else ArrayUtils.EMPTY_STRING_ARRAY
    var hasId = false
    val returnTypes = ArrayList<String>()
    for (methodHeaderDeclaration in methodHeaders) {
        var returnType: String? = null
        if (!possibleClassNames.isEmpty() && !possibleClassNames.contains(methodHeaderDeclaration.containingClassName)) {
            continue
        }
        if (methodHeaderDeclaration is ObjJMethodHeader) {
            returnType = ObjJMethodPsiUtils.getReturnType(methodHeaderDeclaration as ObjJMethodHeader, follow)
        }
        if (returnType == null) {
            continue
        }
        if (returnType == "id") {
            hasId = true
            continue
        }
        if (!returnTypes.contains(returnType)) {
            returnTypes.add(returnType)
        } else if (returnType != UNDETERMINED) {
            return returnType
        }
    }
    return if (hasId) "id" else defaultReturnType
}

fun getReturnTypeFromFormalVariableType(variableName: ObjJVariableName): List<String> {
    val project = variableName.project
    val variableNameText = variableName.qualifiedNameText ?: return ArrayUtils.EMPTY_STRING_ARRAY
    if (variableName.containingClass != null) {
        for (instanceVariableDeclaration in ObjJInstanceVariablesByClassIndex.instance.get(variableName.containingClassName, project)) {
            if (instanceVariableDeclaration.variableName?.text == variableNameText) {
                return ObjJInheritanceUtil.getAllInheritedClasses(instanceVariableDeclaration.formalVariableType.text, project)
            }
        }
    }
    val methodDeclaration = variableName.getParentOfType(ObjJMethodDeclaration::class.java) ?: return ArrayUtils.EMPTY_STRING_ARRAY
    val methodHeaderVariable = ObjJMethodPsiUtils.getHeaderVariableNameMatching(methodDeclaration.methodHeader, variableNameText)
    if (methodHeaderVariable != null) {
        val className = methodHeaderVariable.getParentOfType(ObjJMethodDeclarationSelector::class.java)?.formalVariableType?.className?.text
        if (className != null) {
            return ObjJInheritanceUtil.getAllInheritedClasses(className, methodHeaderVariable.project)
        }
    }
    return ArrayUtils.EMPTY_STRING_ARRAY
}

private fun getSelfOrSuper(expr: ObjJExpr): String? {
    if (expr.text == "self") {
        return ObjJPsiImplUtil.getContainingClassName(expr)
    } else if (expr.text == "super") {
        return ObjJPsiImplUtil.getContainingSuperClassName(expr)
    }
    return null
}


fun ObjJExpr.getVariableNameType(): List<String> {
    val out = ArrayList<String>()
    for (currentExpr in getAllSubExpressions(expr, false)) {
        if (currentExpr.leftExpr == null) {
            continue
        }
        if (isEquivalentTo(currentExpr)) {
            continue
        }
        val project = project
        val reference = currentExpr.leftExpr!!.qualifiedReference ?: continue
        if (reference.text == "self") {
            return ObjJInheritanceUtil.getAllInheritedClasses(ObjJHasContainingClassPsiUtil.getContainingClassName(reference), project)
        }
        if (reference.text == "super") {
            val className = ObjJHasContainingClassPsiUtil.getContainingClassName(reference)
            val classNames = ObjJInheritanceUtil.getAllInheritedClasses(className, project)
            classNames.remove(className)
            return classNames
        }
        val lastVar : ObjJVariableName  = reference.getLastVar() ?: continue
        val fqName = ObjJVariableNameUtil.getQualifiedNameAsString(lastVar)
        //TODO check for whether parameter should be lastVar or currentExpr
        for (variableAssignment in ObjJVariableAssignmentsPsiUtil.getAllVariableAssignmentsMatchingName(lastVar, fqName)) {
            val assignedValue = variableAssignment.assignedValue
            if (assignedValue.isEquivalentTo(currentExpr)) {
                continue
            }
            try {
                val returnType = getReturnType(assignedValue, true)
                if (returnType != null) {
                    out.add(returnType);
                }
            } catch (e: MixedReturnTypeException) {
                for (varType in e.returnTypesList) {
                    if (!out.contains(varType)) {
                        out.add(varType)
                    }
                }
            }

        }
    }
    return out
}

/*
public static List<String> getVariableNameType(@NotNull ObjJExpr expr) {
    List<String> out = new ArrayList<>();
    for (ObjJExpr currentExpr : getAllSubExpressions(expr)) {
        if (currentExpr == null || currentExpr.getLeftExpr() == null) {
            continue;
        }
        ObjJQualifiedReference reference = currentExpr.getLeftExpr().getQualifiedReference();
        if (reference == null) {
            continue;
        }
        if (reference.getLastVar() == null) {
            continue;
        }
        final String fqName = ObjJVariableNameUtil.getQualifiedNameAsString(reference.getLastVar());
        List<ObjJVariableName> elements = ObjJVariableNameUtil.getAndFilterSiblingVariableNameElements(expr, -1, (varNameElement) -> ObjJVariableNameUtil.getQualifiedNameAsString(varNameElement).equals(fqName));
        for (ObjJVariableName variableName : elements) {
            ObjJVariableDeclaration declaration = variableName.getParentOfType(ObjJVariableDeclaration.class);
            if (declaration == null) {
                continue;
            }
            boolean isAssigned = false;
            for (ObjJQualifiedReference qualifiedReference : declaration.getQualifiedReferenceList()) {
                if (variableName.getParent().isEquivalentTo(qualifiedReference)) {
                    isAssigned = true;
                    break;
                }
            }
            if (isAssigned) {
                try {
                    out.add(getReturnType(declaration.getExpr(), null));
                } catch (MixedReturnTypeException e) {
                    for (String varType : e.getReturnTypesList()) {
                        if (!out.contains(varType)) {
                            out.add(varType);
                        }
                    }
                }
            }
        }
    }
    return out;
}
*/

class MixedReturnTypeException internal constructor(val returnTypesList: List<String>) : Exception("More than one return type found")

class ExpressionReturnTypeResults private constructor(val references: MutableList<ExpressionReturnTypeReference>, private val project: Project) {
    private var changed = false
    private var referencedAncestors: MutableList<String>? = null

    val inheritanceUpAndDown: List<String>
        get() {
            if (referencedAncestors != null && !changed) {
                return referencedAncestors as MutableList<String>
            } else if (DumbService.isDumb(project)) {
                return ArrayUtils.EMPTY_STRING_ARRAY
            } else {
                referencedAncestors = ArrayList()
            }
            changed = false
            for (result in references) {
                if (isPrimitive(result.type)) {
                    continue
                }
                getInheritanceUpAndDown(referencedAncestors!!, result.type)
            }
            return referencedAncestors as ArrayList<String>
        }

    constructor(project: Project) : this(ArrayList<ExpressionReturnTypeReference>(), project) {}

    fun tick(refs: List<String>) {
        for (ref in refs) {
            tick(ref)
        }
    }

    private fun tick(ref: String, ticks: Int) {
        var ticks = ticks

        var refObject = getReference(ref)
        if (refObject == null) {
            tick(ref)
            ticks -= 1
            refObject = getReference(ref)
        }
        assert(refObject != null)
        refObject!!.references += ticks
    }

    fun tick(results: ExpressionReturnTypeResults) {
        for (ref in results.references) {
            tick(ref.type, ref.references)
        }
    }

    fun tick(ref: String) {
        if (ref.isEmpty()) {
            return
        }
        var result = getReference(ref)
        if (result != null) {
            result.tick()
        } else {
            changed = true
            result = ExpressionReturnTypeReference(ref)
            references.add(result)
        }
    }

    fun getReference(ref: String): ExpressionReturnTypeReference? {
        if (ref.isEmpty()) {
            return null
        }
        for (result in references) {
            if (result.type == ref) {
                return result
            }
        }
        return null
    }

    fun isReferenced(ref: String): Boolean {
        return getReference(ref) != null || inheritanceUpAndDown.contains(ref)
    }

    private fun getInheritanceUpAndDown(referencedAncestors: MutableList<String>, className: String) {
        if (referencedAncestors.contains(className)) {
            return
        }
        for (currentClassName in ObjJInheritanceUtil.getInheritanceUpAndDown(className, project)) {
            if (!referencedAncestors.contains(currentClassName)) {
                referencedAncestors.add(currentClassName)
            }
        }
    }

    fun numReferences(ref: String): Int {
        val result = getReference(ref)
        return result?.references ?: 0
    }

}

class ExpressionReturnTypeReference internal constructor(val type: String) {
    var references: Int = 0
        internal set

    init {
        references = 1
    }

    internal fun tick(): Int {
        return ++references
    }
}
