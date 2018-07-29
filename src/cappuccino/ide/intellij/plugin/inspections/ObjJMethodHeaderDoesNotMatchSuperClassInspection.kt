package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.fixes.ObjJChangeVarTypeToMatchQuickFix
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.ObjJVisitor
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

    override fun    buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ObjJVisitor() {
            override fun visitMethodHeader(header: ObjJMethodHeader) {
                validateMethodHeader(header, holder)
            }
        }
    }

    companion object {
        private fun validateMethodHeader(header:ObjJMethodHeader, problemsHolder: ProblemsHolder) {
            val inheritedClasses = ObjJInheritanceUtil.getAllInheritedClasses(header.containingClassName,header.project, true)
            val selectorString = header.selectorString
            val matchingMethodHeaders = ObjJUnifiedMethodIndex.instance[selectorString, header.project]
            if (matchingMethodHeaders.isEmpty()) {
                return
            }
            for(aHeader in matchingMethodHeaders) {
                if (!inheritedClasses.contains(aHeader.containingClassName) || aHeader !is ObjJMethodHeader || aHeader.isEquivalentTo(header)) {
                    continue
                }
                if (!matches(header, aHeader, problemsHolder)) {
                    problemsHolder.registerProblem(header, "Incompatible inherited method override")
                    return
                }
            }
        }
        private fun matches(thisHeader:ObjJMethodHeader, thatHeader:ObjJMethodHeader, problemsHolder: ProblemsHolder) : Boolean {
            var matches = true
            if (thisHeader.returnType != thatHeader.returnType) {
                val methodHeaderReturnTypeElement = thisHeader.methodHeaderReturnTypeElement
                if (methodHeaderReturnTypeElement != null) {
                    val thisHeaderReturnType = methodHeaderReturnTypeElement.formalVariableType
                    if (!(thisHeaderReturnType.text?.toLowerCase() != "void" && thatHeader.returnType.toLowerCase() == "id")) {
                        problemsHolder.registerProblem(thisHeaderReturnType, "Overridden parent is less specific", ProblemHighlightType.INFORMATION, ObjJChangeVarTypeToMatchQuickFix(thisHeaderReturnType, thatHeader.returnType))
                    } else {
                        problemsHolder.registerProblem(thisHeader.methodHeaderReturnTypeElement!!, "Overridden method should have return type <" + thatHeader.returnType + ">", ObjJChangeVarTypeToMatchQuickFix(thisHeader, thatHeader.returnType))
                        matches = false
                    }
                } else {
                    matches = false
                }
            }
            val thoseSelectors = thatHeader.methodDeclarationSelectorList

            for ((i, selector) in thisHeader.methodDeclarationSelectorList.withIndex()) {
                val otherParam = thoseSelectors[i].varType ?: continue
                val thisVarType = selector.varType
                if (!Objects.equals(thisVarType?.text,otherParam.text)) {
                    if (thisVarType != null && !(thisVarType.text?.toLowerCase() != "void" && otherParam.text.toLowerCase() == "id")) {
                        problemsHolder.registerProblem(thisVarType, "Overrides parent type has less specific variable type of id")
                    } else {
                        val errorMessage = "Parameter should have type <" + otherParam.text + ">";
                        if (thisVarType != null) {
                            problemsHolder.registerProblem((thisVarType), errorMessage, ObjJChangeVarTypeToMatchQuickFix(thisVarType, otherParam.text))
                        } else if (selector.openParen != null && selector.closeParen != null) {
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