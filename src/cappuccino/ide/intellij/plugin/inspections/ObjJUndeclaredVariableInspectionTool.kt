package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.contributor.*
import cappuccino.ide.intellij.plugin.fixes.*
import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.*
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJIterationStatement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.references.ObjJCommentEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import cappuccino.ide.intellij.plugin.references.ObjJVariableReference
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInspection.*
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor

class ObjJUndeclaredVariableInspectionTool : LocalInspectionTool() {

    override fun runForWholeFile() = true

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : ObjJVisitor() {
            override fun visitVariableName(variableName: ObjJVariableName) {
                super.visitVariableName(variableName)
                registerProblemIfVariableIsNotDeclaredBeforeUse(variableName, holder)
            }
        }
    }

    companion object {

        private fun registerProblemIfVariableIsNotDeclaredBeforeUse(variableNameIn: ObjJVariableName, problemsHolder:ProblemsHolder) {
            var variableName: ObjJVariableName? = variableNameIn

            if (variableName?.getParentOfType(ObjJInstanceVariableList::class.java) != null) {
                return
            }
            // Check if is global variable assignment
            if (variableNameIn.parent is ObjJGlobalVariableDeclaration)
                return

            // if is variable assignment target
            // Have unintended global variable inspection handle it
            if(variableNameIn.parent.parent is ObjJVariableDeclaration)
                return

            if (variableName?.getPreviousNonEmptySibling(true).elementType == ObjJTypes.ObjJ_DOT) {
                return
            }

            if (variableName?.parent is ObjJQualifiedReference) {
                variableName = (variableName.parent!! as ObjJQualifiedReference).primaryVar
            }
            if (variableName == null) {
                return
            }

            if (STATIC_VAR_NAMES.contains(variableName.text)) {
                return
            }

            if (isItselfAVariableDeclaration(variableName)) {
                return
            }

            val project = variableNameIn.project
            if (DumbService.isDumb(project)) {
                return
            }
            val variableNameString = variableNameIn.text

            if (JsTypeDefPropertiesByNamespaceIndex.instance.containsKey(variableNameString, project))
                return

            if (JsTypeDefFunctionsByNamespaceIndex.instance.containsKey(variableNameString, project))
                return

            if (JsTypeDefClassesByNamespaceIndex.instance.containsKey(variableNameString, project))
                return

            if (ObjJClassDeclarationsIndex.instance[variableName.text, variableName.project].isNotEmpty()) {
                return
            }

            if (isDeclaredInEnclosingScopesHeader(variableName)) {
                return
            }

            if (isVariableDeclaredBeforeUse(variableName)) {
                ////LOGGER.info("Variable is <" + variableName.getText() + "> declared before use.");
                return
            }

            if (isJsStandardVariable(variableName)) {
                return
            }

            if (ObjJPluginSettings.isIgnoredVariableName(variableName.text)) {
                problemsHolder.registerProblem(variableName, ObjJBundle.message("objective-j.inspection.undec-var.ignoring.message"), ProblemHighlightType.INFORMATION, ObjJAlterIgnoredUndeclaredVariable(variableName.text, false))
                return
            }

            if (ObjJCommentEvaluatorUtil.isIgnored(variableName, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_VAR, variableName.text)) {
                return
            }

            /*var tempElement = variableName.getNextNonEmptySibling(true)
            if (tempElement != null && tempElement.text == ".") {
                tempElement = tempElement.getNextNonEmptySibling(true)
                if (tempElement is ObjJFunctionCall) {
                    val functionCall = tempElement as ObjJFunctionCall?
                    if (functionCall!!.functionName != null && functionCall.functionName!!.text == "call") {
                        if (ObjJFunctionsIndex.instance[variableName.name, variableName.project].isEmpty()) {
                            problemsHolder.registerProblem(variableName, ObjJBundle.message("objective-j.inspection.undec-var.failed-to-find.message", variableName.name))
                        }
                        return
                    }
                }
            }*/

            val declarations: MutableList<ObjJGlobalVariableDeclaration> = ObjJGlobalVariableNamesIndex.instance[variableName.text, variableName.project].toMutableList()
            if (declarations.isNotEmpty()) {
                return
            }
            if (variableNameString.substring(0, 1) == variableNameString.substring(0, 1).toUpperCase() && variableNameString != variableNameString.toUpperCase()) {
                if (ObjJCommentEvaluatorUtil.isIgnored(variableName, ObjJSuppressInspectionFlags.IGNORE_CLASS, variableNameString) || ObjJPluginSettings.isIgnoredClassName(variableNameString))
                    return
                problemsHolder.registerProblem(
                        variableName,
                        ObjJBundle.message("objective-j.inspection.undec-var.class-may-not-have-been-declared.message"),
                        ObjJAlterIgnoredClassNames(variableNameString, true),
                        ObjJAddSuppressInspectionForScope(variableName, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_CLASS, ObjJSuppressInspectionScope.METHOD, variableNameString),
                        ObjJAddSuppressInspectionForScope(variableName, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_CLASS, ObjJSuppressInspectionScope.FUNCTION, variableNameString),
                        ObjJAddSuppressInspectionForScope(variableName, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_CLASS, ObjJSuppressInspectionScope.CLASS, variableNameString),
                        ObjJAddSuppressInspectionForScope(variableName, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_CLASS, ObjJSuppressInspectionScope.FILE, variableNameString)
                )
                return
            }

            if (variableName.hasText("self") || variableName.hasText("super")) {
                return
            }
            problemsHolder.registerProblem(variableName, ObjJBundle.message("objective-j.inspection.undec-var.var-may-not-have-been-declared.message"),
                    ObjJSuppressUndeclaredVariableInspectionOnVariable(variableName),
                    ObjJAddSuppressInspectionForScope(variableName, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_VAR, ObjJSuppressInspectionScope.METHOD),
                    ObjJAddSuppressInspectionForScope(variableName, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_VAR, ObjJSuppressInspectionScope.FUNCTION),
                    ObjJAddSuppressInspectionForScope(variableName, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_VAR, ObjJSuppressInspectionScope.CLASS),
                    ObjJAddSuppressInspectionForScope(variableName, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_VAR, ObjJSuppressInspectionScope.FILE),
                    ObjJAlterIgnoredUndeclaredVariable(variableName.text, addToIgnored = true))

        }

        private fun isVariableDeclaredBeforeUse(variableName: ObjJVariableName): Boolean {
            if (ObjJKeywordsList.keywords.contains(variableName.text)) {
                return true
            }
            val resolved = ObjJVariableReference(variableName).resolve(nullIfSelfReferencing = true)
            if (resolved != null) {
                return true//!isDeclaredInSameDeclaration(variableName, resolved)
            }
            if (ObjJVariableReference(variableName).multiResolve(false).isNotEmpty())
                return true
            val precedingVariableNameReferences = ObjJVariableNameResolveUtil.getMatchingPrecedingVariableNameElements(variableName, 0)
            return precedingVariableNameReferences.isNotEmpty() || ObjJFunctionsIndex.instance[variableName.text, variableName.project].isNotEmpty()
        }

        private fun isDeclaredInSameDeclaration(variableName: ObjJVariableName, resolved:PsiElement) : Boolean {
            if (variableName.isEquivalentTo(resolved))
                return false
            val resolvedDeclaration = resolved.getParentOfType(ObjJVariableDeclaration::class.java) ?: return false
            val thisVariableDeclaration = variableName.getParentOfType(ObjJVariableDeclaration::class.java) ?: return false
            return resolvedDeclaration.isEquivalentTo(thisVariableDeclaration)
        }

        private fun isJsStandardVariable(variableName: ObjJVariableName): Boolean {
            val variableNameText = variableName.text
            return JsTypeDefPropertiesByNamespaceIndex.instance.containsKey(variableNameText, variableName.project) || JsTypeDefFunctionsByNamespaceIndex.instance.containsKey(variableNameText, variableName.project)
        }

        private fun isDeclaredInEnclosingScopesHeader(variableName: ObjJVariableName): Boolean {
            return ObjJVariableNameAggregatorUtil.isInstanceVarDeclaredInClassOrInheritance(variableName) ||
                    isDeclaredInContainingMethodHeader(variableName) ||
                    isDeclaredInFunctionScope(variableName) ||
                    ObjJVariableNameResolveUtil.getMatchingPrecedingVariableNameElements(variableName, 0).isNotEmpty()
        }

        private fun isDeclaredInContainingMethodHeader(variableName: ObjJVariableName): Boolean {
            val methodDeclaration = variableName.getParentOfType(ObjJMethodDeclaration::class.java)
            return methodDeclaration != null && ObjJMethodPsiUtils.getHeaderVariableNameMatching(methodDeclaration.methodHeader, variableName.text) != null
        }

        private fun isDeclaredInFunctionScope(variableName: ObjJVariableName): Boolean {
            val functionDeclarationElement = variableName.getParentOfType(ObjJFunctionDeclarationElement::class.java)
            if (functionDeclarationElement != null) {
                for (ob in functionDeclarationElement.formalParameterArgList) {
                    if (ob.variableName?.text == variableName.text) {
                        return true
                    }
                }
            }
            return false
        }

        private fun isItselfAVariableDeclaration(variableName: ObjJVariableName): Boolean {
            //If variable name is itself an instance variable
            if (variableName.parent is ObjJInstanceVariableDeclaration) {
                return true
            }

            if (variableName.parent is ObjJVariableDeclarationList) {
                return true
            }

            if (variableName.parent is ObjJInstanceVariableList) {
                return true
            }

            //If variable name element is itself a method header declaration variable
            if (variableName.getParentOfType(ObjJMethodHeaderDeclaration::class.java) != null) {
                return true
            }

            if (variableName.parent is ObjJGlobalVariableDeclaration) {
                return true
            }

            //If variable name is itself an function variable
            if (variableName.parent is ObjJFormalParameterArg) {
                return true
            }

            //If variable name itself is declared in catch header in try/catch block
            if (variableName.parent is ObjJCatchProduction) {
                return true
            }
            //If variable name itself a javascript object property name
            if (variableName.parent is ObjJPropertyAssignment) {
                return true
            }

            if (variableName.parent is ObjJInExpr) {
                return true
            }

            if (variableName.getParentOfType(ObjJPreprocessorDefineFunction::class.java) != null) {
                return true
            }

            if (variableName.parent is ObjJGlobal) {
                return true
            }


            val parent = variableName.parent
            if (parent is ObjJBodyVariableAssignment && parent.varModifier != null) {
                return true
            }

            val reference = variableName.getParentOfType(ObjJQualifiedReference::class.java) ?: return false

            if (reference.parent is ObjJVariableDeclaration) {
                val variableAssignment = variableName.getParentOfType(ObjJBodyVariableAssignment::class.java)
                if (variableAssignment != null) {
                    return variableAssignment.varModifier != null
                }
            }

            if (reference.parent is ObjJVariableDeclaration && !isDeclaredInSameDeclaration(variableName, reference)) {
                if(variableName.getParentOfType(ObjJForLoopPartsInBraces::class.java)?.varModifier != null ||
                        variableName.getParentOfType(ObjJInExpr::class.java)?.varModifier != null) {
                    return true
                }
            }


            if (reference.parent is ObjJIterationStatement) {
                return true
            }

            var assignment: ObjJBodyVariableAssignment? = null
            if (reference.parent is ObjJVariableDeclaration) {
                val variableDeclaration = reference.parent as ObjJVariableDeclaration
                if (variableDeclaration.parent is ObjJIterationStatement && variableDeclaration.siblingOfTypeOccursAtLeastOnceBefore(ObjJVarModifier::class.java)) {
                    return true
                } else if (variableDeclaration.parent is ObjJGlobalVariableDeclaration) {
                    return true
                }
                assignment = if (variableDeclaration.parent is ObjJBodyVariableAssignment) variableDeclaration.parent as ObjJBodyVariableAssignment else null
            }
            return assignment != null && assignment.varModifier != null
        }
    }

}
