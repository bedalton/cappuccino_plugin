package cappuccino.ide.intellij.plugin.annotator

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.lang.ObjJSyntaxHighlighter
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.getParentOfType
import cappuccino.ide.intellij.plugin.psi.utils.tokenType
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement

class ObjJSyntaxHighlighterAnnotator : Annotator {
    override fun annotate(
            psiElement: PsiElement,
            annotationHolder: AnnotationHolder) {
        when (psiElement) {
            is ObjJFormalVariableType -> annotateFormalVariableType(psiElement, annotationHolder)
            is ObjJClassName -> highlightClassName(psiElement, annotationHolder)
            is ObjJVariableName -> highlightVariableName(psiElement, annotationHolder)
            else -> when (psiElement.tokenType()) {
                ObjJTypes.ObjJ_VAR_TYPE_BOOL,
                ObjJTypes.ObjJ_VAR_TYPE_INT,
                ObjJTypes.ObjJ_VAR_TYPE_SHORT,
                ObjJTypes.ObjJ_VAR_TYPE_LONG,
                ObjJTypes.ObjJ_VAR_TYPE_LONG_LONG,
                ObjJTypes.ObjJ_VAR_TYPE_UNSIGNED,
                ObjJTypes.ObjJ_VAR_TYPE_SIGNED,
                ObjJTypes.ObjJ_VAR_TYPE_FLOAT,
                ObjJTypes.ObjJ_VAR_TYPE_DOUBLE,
                ObjJTypes.ObjJ_VAR_TYPE_BYTE,
                ObjJTypes.ObjJ_VAR_TYPE_ID -> stripVarTypesAnnotationIfNotInValidBlock(psiElement, annotationHolder)

                ObjJTypes.ObjJ_IF,
                ObjJTypes.ObjJ_ELSE,
                ObjJTypes.ObjJ_IN,
                ObjJTypes.ObjJ_FOR,
                ObjJTypes.ObjJ_WHILE,
                ObjJTypes.ObjJ_DO -> stripLoopWordsIfInSelector(psiElement, annotationHolder)
            }
        }
    }

    private fun highlightVariableName(variableNameElement:ObjJVariableName, annotationHolder: AnnotationHolder) {
        val project = variableNameElement.project
        if (DumbService.isDumb(project)) {
            DumbService.getInstance(project).runReadActionInSmartMode({
                highlightVariableName(variableNameElement, annotationHolder)
            })
            return
        }
        val variableName = variableNameElement.text
        if (variableName == "self" || variableName == "super" || variableName == "this") {
            colorize(variableNameElement, annotationHolder, ObjJSyntaxHighlighter.KEYWORD)
        }
        val parent = variableNameElement.parent
        if (parent is ObjJQualifiedReference) {
            val index = parent.variableNameList.indexOf(variableNameElement)
            if (index == 0) {
                if (!ObjJClassDeclarationsIndex.instance[variableName, project].isEmpty()) {
                    colorize(variableNameElement, annotationHolder, ObjJSyntaxHighlighter.VARIABLE_TYPE)
                    return
                }
            }
            val firstIn = parent.variableNameList[0].text
            if (index == 0 || (index == 1 && (firstIn == "self" || firstIn == "super"))) {
                if (ObjJInheritanceUtil.isInstanceVariableInClasses(variableName, variableNameElement.containingClassName, variableNameElement.project)) {
                    colorize(variableNameElement, annotationHolder, ObjJSyntaxHighlighter.INSTANCE_VAR)
                    return
                }
            }

        }
    }

    private fun highlightClassName(classNameElement:ObjJClassName, annotationHolder: AnnotationHolder) {
        colorize(classNameElement, annotationHolder, ObjJSyntaxHighlighter.VARIABLE_TYPE)
    }

    private fun stripVarTypesAnnotationIfNotInValidBlock(psiElement: PsiElement, annotationHolder: AnnotationHolder) {
        if (psiElement.getParentOfType(ObjJBlock::class.java) != null) {
            stripAnnotation(psiElement, annotationHolder)
            return
        }
        if (psiElement.getParentOfType(ObjJInstanceVariableList::class.java) == null && psiElement.getParentOfType(ObjJMethodDeclarationSelector::class.java) == null) {
            stripAnnotation(psiElement, annotationHolder)
        }
    }

    private fun stripLoopWordsIfInSelector(psiElement: PsiElement, annotationHolder: AnnotationHolder) {
        if (psiElement.getParentOfType(ObjJMethodCall::class.java) != null) {
            stripAnnotation(psiElement, annotationHolder)
        }
    }

    private fun annotateFormalVariableType(type:ObjJFormalVariableType, annotationHolder: AnnotationHolder) {
        colorize(type, annotationHolder, ObjJSyntaxHighlighter.VARIABLE_TYPE)
    }

    private fun stripAnnotation(psiElement: PsiElement, annotationHolder: AnnotationHolder) {
        annotationHolder.createInfoAnnotation(psiElement, "").enforcedTextAttributes = TextAttributes.ERASE_MARKER
    }

    private fun colorize(psiElement: PsiElement, annotationHolder: AnnotationHolder, attribute:TextAttributesKey) {
        annotationHolder.createInfoAnnotation(psiElement, "").textAttributes = attribute
    }
}