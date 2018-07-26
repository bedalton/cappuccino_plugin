package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.ObjJVisitor
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import com.intellij.codeInspection.LocalInspectionTool
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
                }
            }
        }
        private fun matches(thisHeader:ObjJMethodHeader, thatHeader:ObjJMethodHeader, problemsHolder: ProblemsHolder) : Boolean {
            if (thisHeader.returnType != thatHeader.returnType) {
                if (thisHeader.methodHeaderReturnTypeElement != null) {
                    problemsHolder.registerProblem(thisHeader.methodHeaderReturnTypeElement!!, "Overridden method should have return type <" + thatHeader.returnType + ">")
                }
                return false
            }
            val thoseParams = thatHeader.paramTypes
            var matches = true;
            for ((i, param) in thisHeader.paramTypes.withIndex()) {
                val otherParam = thoseParams[i] ?: continue;
                if (!Objects.equals(param?.text,otherParam.text)) {
                    if (param != null ) {
                        problemsHolder.registerProblem((param.className ?: param), "Parameter should have type <" + otherParam.text + ">")
                    }
                    matches = false
                }
            }
            return matches
        }
    }

}