package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.fixes.ObjJAddSuppressInspectionForScope
import cappuccino.ide.intellij.plugin.fixes.ObjJChangeVarTypeToMatchQuickFix
import cappuccino.ide.intellij.plugin.fixes.ObjJSuppressInspectionScope
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.ObjJVisitor
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.ObjJCommentParserUtil
import cappuccino.ide.intellij.plugin.psi.utils.ObjJSuppressInspectionFlags
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import java.util.*

class ObjJMethodHeaderDoesNotMatchSuperClassInspection : LocalInspectionTool() {

    override fun getShortName(): String {
        return "MethodHeaderDoesNotMatchSuperClass"
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ObjJVisitor() {
            override fun visitMethodHeader(header: ObjJMethodHeader) {
                validateMethodHeader(header, holder)
            }
        }
    }

    companion object {
        private fun validateMethodHeader(header: ObjJMethodHeader, problemsHolder: ProblemsHolder) {

            if (ObjJCommentParserUtil.isIgnored(header, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE)) {
                return
            }
            val inheritedClasses = ObjJInheritanceUtil.getAllInheritedClasses(header.containingClassName, header.project, true)
            val selectorString = header.selectorString
            val matchingMethodHeaders = ObjJUnifiedMethodIndex.instance[selectorString, header.project]
            if (matchingMethodHeaders.isEmpty()) {
                return
            }
            for (aHeader in matchingMethodHeaders) {
                if (!inheritedClasses.contains(aHeader.containingClassName) || aHeader !is ObjJMethodHeader || aHeader.isEquivalentTo(header)) {
                    continue
                }
                if (!matches(header, aHeader, problemsHolder)) {
                    problemsHolder.registerProblem(header, "Incompatible inherited method override",
                            ObjJAddSuppressInspectionForScope(header, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.STATEMENT),
                            ObjJAddSuppressInspectionForScope(header, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.METHOD),
                            ObjJAddSuppressInspectionForScope(header, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.FUNCTION),
                            ObjJAddSuppressInspectionForScope(header, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.CLASS),
                            ObjJAddSuppressInspectionForScope(header, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.FILE))
                    return
                }
            }
        }

        private fun matches(thisHeader: ObjJMethodHeader, thatHeader: ObjJMethodHeader, problemsHolder: ProblemsHolder): Boolean {
            if (ObjJCommentParserUtil.isIgnored(thisHeader, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE)) {
                return true
            }
            var matches = true
            if (thisHeader.returnType != thatHeader.returnType) {
                val methodHeaderReturnTypeElement = thisHeader.methodHeaderReturnTypeElement
                if (methodHeaderReturnTypeElement != null) {
                    val thisHeaderReturnType = methodHeaderReturnTypeElement.formalVariableType
                    matches = if (methodHeaderReturnTypeElement.formalVariableType.varTypeId != null && thatHeader.methodHeaderReturnTypeElement?.formalVariableType?.varTypeId == null) {
                        problemsHolder.registerProblem(thisHeaderReturnType, "Method return type is less specific than parent class", ProblemHighlightType.INFORMATION, ObjJChangeVarTypeToMatchQuickFix(thisHeaderReturnType, thatHeader.returnType),
                                ObjJAddSuppressInspectionForScope(methodHeaderReturnTypeElement, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.STATEMENT),
                                ObjJAddSuppressInspectionForScope(methodHeaderReturnTypeElement, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.METHOD),
                                ObjJAddSuppressInspectionForScope(methodHeaderReturnTypeElement, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.FUNCTION),
                                ObjJAddSuppressInspectionForScope(methodHeaderReturnTypeElement, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.CLASS),
                                ObjJAddSuppressInspectionForScope(methodHeaderReturnTypeElement, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.FILE))
                        false
                    } else if (ObjJClassType.isSubclassOrSelf(thatHeader.methodHeaderReturnTypeElement?.text
                                    ?: "", thisHeader.methodHeaderReturnTypeElement?.text ?: "", thisHeader.project)) {
                        true
                    } else {
                        problemsHolder.registerProblem(methodHeaderReturnTypeElement, "Overridden method should have return type <" + thatHeader.returnType + ">", ObjJChangeVarTypeToMatchQuickFix(thisHeader, thatHeader.returnType),
                                ObjJAddSuppressInspectionForScope(methodHeaderReturnTypeElement, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.STATEMENT),
                                ObjJAddSuppressInspectionForScope(methodHeaderReturnTypeElement, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.METHOD),
                                ObjJAddSuppressInspectionForScope(methodHeaderReturnTypeElement, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.FUNCTION),
                                ObjJAddSuppressInspectionForScope(methodHeaderReturnTypeElement, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.CLASS),
                                ObjJAddSuppressInspectionForScope(methodHeaderReturnTypeElement, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.FILE))
                        false
                    }
                } else {
                    matches = false
                }
            }
            val thoseSelectors = thatHeader.methodDeclarationSelectorList

            for ((i, selector) in thisHeader.methodDeclarationSelectorList.withIndex()) {
                val otherParam = thoseSelectors[i].varType ?: continue
                val thisVarType = selector.varType
                if (!Objects.equals(thisVarType?.text, otherParam.text)) {
                    if (thisVarType != null && (thisVarType.text?.toLowerCase() != "void" && otherParam.varTypeId != null)) {
                        problemsHolder.registerProblem(thisVarType, "Overridden parent is less specific", ProblemHighlightType.INFORMATION, ObjJChangeVarTypeToMatchQuickFix(thisVarType, otherParam.text),
                                ObjJAddSuppressInspectionForScope(thisVarType, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.STATEMENT),
                                ObjJAddSuppressInspectionForScope(thisVarType, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.METHOD),
                                ObjJAddSuppressInspectionForScope(thisVarType, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.FUNCTION),
                                ObjJAddSuppressInspectionForScope(thisVarType, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.CLASS),
                                ObjJAddSuppressInspectionForScope(thisVarType, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.FILE))
                    } else {
                        val errorMessage = "Parameter should have type <" + otherParam.text + ">";
                        if (thisVarType != null) {
                            problemsHolder.registerProblem((thisVarType), errorMessage, ObjJChangeVarTypeToMatchQuickFix(thisVarType, otherParam.text),
                                    ObjJAddSuppressInspectionForScope(thisVarType, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.STATEMENT),
                                    ObjJAddSuppressInspectionForScope(thisVarType, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.METHOD),
                                    ObjJAddSuppressInspectionForScope(thisVarType, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.FUNCTION),
                                    ObjJAddSuppressInspectionForScope(thisVarType, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.CLASS),
                                    ObjJAddSuppressInspectionForScope(thisVarType, ObjJSuppressInspectionFlags.IGNORE_INCOMPATIBLE_METHOD_OVERRIDE, ObjJSuppressInspectionScope.FILE))
                        } else if (selector.methodHeaderSelectorFormalVariableType?.openParen != null && selector.methodHeaderSelectorFormalVariableType?.closeParen != null) {
                            problemsHolder.registerProblem(selector, errorMessage, ObjJChangeVarTypeToMatchQuickFix(selector, otherParam.text))
                        } else {
                            problemsHolder.registerProblem(selector, errorMessage)
                        }
                        matches = false
                    }
                }
            }
            return matches
        }
    }

}