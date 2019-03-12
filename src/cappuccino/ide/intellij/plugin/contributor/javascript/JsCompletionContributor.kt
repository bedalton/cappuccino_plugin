package cappuccino.ide.intellij.plugin.contributor.javascript

import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJFunctionNameInsertHandler
import cappuccino.ide.intellij.plugin.psi.ObjJQualifiedReference
import cappuccino.ide.intellij.plugin.psi.ObjJVariableName
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJQualifiedReferenceComponent
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder

object JsCompletionContributor {

    fun appendResults(resultSet:CompletionResultSet, variableName:ObjJVariableName) {
        val qualifiedParts:List<ObjJQualifiedReferenceComponent> =
                variableName.getParentOfType(ObjJQualifiedReference::class.java)?.qualifiedNameParts ?: return
        if (qualifiedParts.isEmpty()) {
            return appendAll(resultSet)
        }
        val indexInQualifiedName = variableName.indexInQualifiedReference
        val first = qualifiedParts[0] ?: return
        if (indexInQualifiedName == 0) {
            return appendAllForFirstElement(resultSet, first.text.removeSuffix("INTELLIJ_IDEA_RULEZ"))
        }

    }

    fun appendAll(resultSet: CompletionResultSet) {
        resultSet.addAllJsFunctions(AllFunction.functions)
        resultSet.addAllJsClasses(JsClasses.classes)
    }

    fun appendAllForFirstElement(resultSet: CompletionResultSet, variableNamePart:String) {
        resultSet.addAllJsFunctions(AllFunction.functions.find(variableNamePart))
        resultSet.addAllJsClasses(JsClasses.classes.find(variableNamePart))
        resultSet.addAllJsProperties(JsClasses.window?.properties ?: listOf())
    }

    fun appendForElementTypes(variableTypes:List<String>) {

    }

}

fun CompletionResultSet.addElement(function:JsFunction) {
    val lookupElement = LookupElementBuilder
            .create(function.functionName)
            .withTailText("(" + ArrayUtils.join(function.params.map { it.propertyName }, ",") + ")")
            .withInsertHandler(ObjJFunctionNameInsertHandler.instance)
    this.addElement(lookupElement)
}

fun CompletionResultSet.addAllJsFunctions(functions:List<JsFunction>) {
    functions.forEach {
        this.addElement(it)
    }
}



fun CompletionResultSet.addElement(jsClass:JsClass) {
    val lookupElement = LookupElementBuilder
            .create(jsClass.className)
            .withTailText("declared in es5.lib")
    this.addElement(lookupElement)
}

fun CompletionResultSet.addAllJsClasses(classes:List<JsClass>) {
    classes.forEach {
        this.addElement(it)
    }
}


fun CompletionResultSet.addElement(property:JsProperty) {
    val lookupElement = LookupElementBuilder
            .create(property.propertyName)
    this.addElement(lookupElement)
}

fun CompletionResultSet.addAllJsProperties(classes:List<JsProperty>) {
    classes.forEach {
        this.addElement(it)
    }
}