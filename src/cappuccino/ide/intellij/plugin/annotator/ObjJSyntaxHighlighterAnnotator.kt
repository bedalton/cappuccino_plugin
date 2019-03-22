package cappuccino.ide.intellij.plugin.annotator

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.lang.ObjJSyntaxHighlighter
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.references.ObjJVariableReference
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil
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
            is ObjJFunctionCall -> highlightFunctionName(psiElement, annotationHolder)
            is ObjJFunctionName -> if (psiElement.hasParentOfType(ObjJFunctionCall::class.java)) colorize(psiElement, annotationHolder, ObjJSyntaxHighlighter.FUNCTION_NAME)
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
            DumbService.getInstance(project).runReadActionInSmartMode {
                highlightVariableName(variableNameElement, annotationHolder)
            }
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
            val referencedVariable:PsiElement? = ObjJVariableReference(variableNameElement).resolve() ?: return
            if (referencedVariable equals variableNameElement) {
                return
            }
            if (referencedVariable.hasParentOfType(ObjJGlobalVariableDeclaration::class.java) || referencedVariable.hasParentOfType(ObjJGlobal::class.java)) {
                colorize(variableNameElement, annotationHolder, ObjJSyntaxHighlighter.GLOBAL_VARIABLE)
                return
            } else if (referencedVariable.hasParentOfType(ObjJInstanceVariableList::class.java)) {
                colorizeInstanceVariable(variableNameElement, referencedVariable, annotationHolder)
                return
            } else if (referencedVariable.hasParentOfType(ObjJMethodDeclarationSelector::class.java) || referencedVariable.hasParentOfType(ObjJFormalParameterArg::class.java)) {
                colorize(variableNameElement, annotationHolder, ObjJSyntaxHighlighter.PARAMETER_VARIABLE)
                return
            } else if (referencedVariable.hasParentOfType(ObjJGlobalVariableDeclaration::class.java) || referencedVariable.getParentOfType(ObjJBodyVariableAssignment::class.java)?.getContainingScope() == ReferencedInScope.FILE) {
                colorize(variableNameElement, annotationHolder, ObjJSyntaxHighlighter.FILE_LEVEL_VARIABLE, "defined in file: "+(referencedVariable ?: variableNameElement).containingFile.originalFile.name)
            }

        }
    }

    private fun colorizeInstanceVariable(variableNameElement:ObjJVariableName, referencedVariable:PsiElement?, annotationHolder: AnnotationHolder) {
        val message:String?
        if (referencedVariable is ObjJVariableName) {
            message = "defined in class: "+referencedVariable.containingClassName
        } else {
            message = null
        }
        colorize(variableNameElement, annotationHolder, ObjJSyntaxHighlighter.INSTANCE_VARIABLE, message)
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

    private fun highlightFunctionName(functionCall: ObjJFunctionCall, annotationHolder: AnnotationHolder) {
        val functionName = functionCall.functionName ?: return
        val resolved = functionName.reference.resolve()
        if (resolved?.getParentOfType(ObjJMethodHeader::class.java) != null) {
            colorize(functionName, annotationHolder, ObjJSyntaxHighlighter.PARAMETER_VARIABLE)
        }
        if (resolved != null) {
            colorize(functionName,annotationHolder, ObjJSyntaxHighlighter.GLOBAL_FUNCTION_NAME, "defined in file: ${ObjJFileUtil.getContainingFileName(functionName)}")
        } else {
            //colorize(functionName,annotationHolder, ObjJSyntaxHighlighter.FUNCTION_NAME, "")
        }

    }

    private fun stripAnnotation(psiElement: PsiElement, annotationHolder: AnnotationHolder) {
        annotationHolder.createInfoAnnotation(psiElement, "").enforcedTextAttributes = TextAttributes.ERASE_MARKER
    }

    private fun colorize(psiElement: PsiElement, annotationHolder: AnnotationHolder, attribute:TextAttributesKey, message:String? = null) {
        annotationHolder.createInfoAnnotation(psiElement, message).textAttributes = attribute
    }

}