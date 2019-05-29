package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.contributor.*
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJQualifiedReferenceComponent
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.ObjJFunctionDeclarationPsiUtil
import cappuccino.ide.intellij.plugin.psi.utils.getBlockChildrenOfType
import cappuccino.ide.intellij.plugin.psi.utils.getParentBlockChildrenOfType
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import cappuccino.ide.intellij.plugin.utils.orElse
import cappuccino.ide.intellij.plugin.utils.substringFromEnd
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ReferencesSearch

internal fun inferQualifiedReferenceType(qualifiedReference: ObjJQualifiedReference, level: Int): InferenceResult? {
    val parts = qualifiedReference.qualifiedNameParts
    if (parts.isEmpty())
        return null
    if (parts.size == 0) {
        return null
    }
    val objjClasses = AllObjJClassesAsJsClasses(qualifiedReference.project)
    var parentTypes: InferenceResult? = null
    var isStatic = false
    for (i in 0 until parts.size) {
        val part = parts[i]
        if (i == 0) {
            parentTypes = getPartTypes(part, parentTypes, false, objjClasses,level - 1)
            isStatic = part.text in globalJSClassNames
        }
        parentTypes = getPartTypes(part, parentTypes, isStatic, objjClasses, level - 1)
        if (isStatic) {
            isStatic = false
        }
    }
    return parentTypes
}

val SPLIT_JS_CLASS_TYPES_LIST_REGEX = "\\s*\\|\\s*".toRegex()

internal fun getPartTypes(part: ObjJQualifiedReferenceComponent, parentTypes: InferenceResult?, static: Boolean, objjClasses:List<GlobalJSClass>, level: Int): InferenceResult? {
    return when (part) {
        is ObjJVariableName -> getVariableNameComponentTypes(part, parentTypes, objjClasses, level)
        is ObjJFunctionCall -> getFunctionComponentTypes(part.functionName, parentTypes, static, level)
        is ObjJFunctionName -> getFunctionComponentTypes(part, parentTypes, static, level)
        is ObjJArrayIndexSelector -> getArrayTypes(part.project, parentTypes)
        is ObjJMethodCall -> inferMethodCallType(part, level - 1)
        else -> return null
    }
}


internal fun getVariableNameComponentTypes(variableName: ObjJVariableName, parentTypes: InferenceResult?,  objjClasses:List<GlobalJSClass>, level: Int): InferenceResult? {
    if (level < 0) {
        return null
    }
    if (variableName.indexInQualifiedReference == 0) {
        return inferVariableNameType(variableName, level)
    }
    if (parentTypes == null)
        return null

    val project = variableName.project
    val variableNameString = variableName.text
    val containingClass = if (variableNameString == "self") {
        variableName.containingClassName
    } else if (variableNameString == "super") {
        variableName.getContainingSuperClass()?.name
    } else
        null
    if (containingClass != null && containingClass != ObjJClassType.UNDEF_CLASS_NAME) {
        return InferenceResult(
                classes = ObjJInheritanceUtil.getAllInheritedClasses(containingClass, project).mapNotNull {
                    getJsClassObject(project, objjClasses, it)
                }
        )
    }

    val classes = if (parentTypes.anyType) {
        getAllObjJAndJsClassObjects(variableName.project)
    } else
        parentTypes.classes
    val classNames = classes.flatMap { jsClass ->
        jsClass.properties.firstOrNull {
            it.name == variableNameString
        }?.type?.split(SPLIT_JS_CLASS_TYPES_LIST_REGEX) ?: emptyList()
    }.toSet()
    return classNames.toInferenceResult(project)
}

internal fun inferVariableNameType(variableName: ObjJVariableName, levels: Int): InferenceResult? {
    if (levels < 0) {
        return null
    }
    if (variableName.indexInQualifiedReference != 0)
        return null
    val referencedVariable = variableName.reference.resolve()
    val assignedExpressions = if (referencedVariable is ObjJVariableName) {
        ReferencesSearch.search(referencedVariable)
                .findAll()
                .mapNotNull { getAssignedExpressions(it.element) }
    } else {
        variableName.getParentBlockChildrenOfType(ObjJVariableName::class.java, true)
                .mapNotNull { getAssignedExpressions(it) }
    }
    val project = variableName.project
    val variableNameString = variableName.text
    val objjClasses = AllObjJClassesAsJsClasses(project)
    val staticVariableNameTypes = ObjJGlobalJSVariables.filter {
        it.name == variableNameString
    }.flatMap { it.type.split(SPLIT_JS_CLASS_TYPES_LIST_REGEX) }.mapNotNull {
        getJsClassObject(project, objjClasses, it)
    }

    val out = getInferredTypeFromExpressionArray(assignedExpressions, levels)
    return if (staticVariableNameTypes.isNotEmpty())
        out.copy(classes = out.classes + staticVariableNameTypes)
    else
        out
}

private fun getAssignedExpressions(element: PsiElement?): ObjJExpr? {
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

private fun getFunctionComponentTypes(functionName: ObjJFunctionName?, parentTypes: InferenceResult?, static:Boolean, level: Int): InferenceResult? {
    if (functionName == null)
        return null
    if (functionName.indexInQualifiedReference == 0) {
        return findFunctionReturnTypesIfFirst(functionName, level)
    }
    if (parentTypes == null) {
        return null
    }
    val functionNameString = functionName.text
    val classes = if (parentTypes.anyType) {
        globalJSClasses
    } else
        parentTypes.classes

    return classes.flatMap { jsClass ->
        (if (static)
            jsClass.staticFunctions.firstOrNull {
                it.name == functionNameString
            }
        else
            jsClass.functions.firstOrNull {
                it.name == functionNameString
            })?.returns?.type?.split(SPLIT_JS_CLASS_TYPES_LIST_REGEX) ?: emptyList()
    }.toInferenceResult(functionName.project)
}

private fun findFunctionReturnTypesIfFirst(functionName: ObjJFunctionName, level: Int): InferenceResult? {
    if (functionName.indexInQualifiedReference != 0) {
        return null
    }
    val functionNameString = functionName.text
    val functionDeclaration = functionName.reference.resolve()?.getParentFunctionDeclaration
    val basicReturnTypes = functionDeclaration?.returnType?.split(SPLIT_JS_CLASS_TYPES_LIST_REGEX) ?: emptyList()

    val returnTypes = globalJsFunctions.filter {
        it.name == functionNameString
    }.flatMap {
        it.returns?.type?.split(SPLIT_JS_CLASS_TYPES_LIST_REGEX) ?: emptyList()
    }
    if (basicReturnTypes.isEmpty() && functionDeclaration != null) {
        val returnStatementExpressions = functionDeclaration.block.getBlockChildrenOfType(ObjJReturnStatement::class.java, true).mapNotNull { it.expr }
        getInferredTypeFromExpressionArray(returnStatementExpressions, level - 1) + returnTypes.toInferenceResult(functionName.project)
    }
    return (returnTypes + basicReturnTypes).toInferenceResult(functionName.project)
}

private val PsiElement.getParentFunctionDeclaration
    get() = ObjJFunctionDeclarationPsiUtil.getParentFunctionDeclaration(this)

private fun Iterable<String>.toInferenceResult(project: Project): InferenceResult {
    val objjClasses = AllObjJClassesAsJsClasses(project)
    val classes = this.mapNotNull {
        getJsClassObject(project, objjClasses, it)
    }
    val arrayClasses = this.filter { it.endsWith("[]") }.map { it.substringFromEnd(0, 2) }
    return InferenceResult(
            isString = this.any { it.toLowerCase() in stringTypes },
            isBoolean = this.any { it.toLowerCase() in booleanTypes },
            isNumeric = this.any { it.toLowerCase() in numberTypes },
            isRegex = this.any { it.toLowerCase() == "regex"},
            isDictionary = this.any { it.toLowerCase() in dictionaryTypes},
            isSelector = this.any { it.toLowerCase() == "sel" },
            arrayTypes = arrayClasses,
            classes = classes
    )
}

private fun getArrayTypes(project:Project, parentTypes: InferenceResult?): InferenceResult? {
    if (parentTypes == null) {
        return INFERRED_ANY_TYPE
    }
    val types = (parentTypes.arrayTypes?.toMutableList() ?: mutableListOf()).filterNot {
        val value = it.toLowerCase()
        value == "null" || value == "nil" || value == "undefined"
    }
    if (parentTypes.arrayTypes?.size.orElse(0) < 1
            && parentTypes.classes.size == 1
            && parentTypes.classes[0].className in stringTypes
    ) {
        return InferenceResult(
                isString = true,
                classes = listOf(JS_STRING),
                arrayTypes = listOf("string")
        )
    }
    if (types.isNotEmpty()) {
        return types.toInferenceResult(project)
    }
    return INFERRED_ANY_TYPE
}