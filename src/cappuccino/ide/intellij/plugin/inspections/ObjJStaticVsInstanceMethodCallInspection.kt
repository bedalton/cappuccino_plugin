package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJUnifiedMethodIndex
import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall
import cappuccino.ide.intellij.plugin.psi.ObjJVisitor
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.getPossibleCallTargetTypes
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElementVisitor

class ObjJStaticVsInstanceMethodCallInspection : LocalInspectionTool() {


    override fun buildVisitor(problemsHolder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ObjJVisitor() {
            override fun visitMethodCall(methodCall: ObjJMethodCall) {
                annotateStaticMethodCall(methodCall, problemsHolder)
                super.visitMethodCall(methodCall)
            }
        }
    }


    companion object {

        /**
         * Validates and annotates static vs instance method calls.
         * @param methodCall method call to annotate
         * @param problemsHolder annotation problemsHolder
         */
        private fun annotateStaticMethodCall(methodCall: ObjJMethodCall, problemsHolder: ProblemsHolder) {
            /*if(IgnoreUtil.shouldIgnore(methodCall, ElementType.METHOD_SCOPE)) {
                return;
            }*/
            val callTarget = methodCall.callTarget.text
            val possibleCallTargetClassTypes = if (ObjJPluginSettings.validateCallTarget()) methodCall.callTarget.getPossibleCallTargetTypes() else null
            if (callTarget == "self" || callTarget == "super" ||
                    possibleCallTargetClassTypes != null && possibleCallTargetClassTypes.contains(ObjJClassType.UNDETERMINED)) {
                return
            }
            if (DumbService.isDumb(methodCall.project)) {
                return
            }
            val isStaticReference = ObjJImplementationDeclarationsIndex.instance[callTarget, methodCall.project].isNotEmpty()
            //Logger.getLogger("ObjJMethodCallAnnotator").log(Level.INFO, "ImplementationDecIndex has: "+ObjJImplementationDeclarationsIndex.instance.getAllKeys(methodCall.project).size + " keys in index")

            val methodHeaderDeclarations = ObjJUnifiedMethodIndex.instance[methodCall.selectorString, methodCall.project]
            for (declaration in methodHeaderDeclarations) {
                ProgressIndicatorProvider.checkCanceled()
                if (possibleCallTargetClassTypes?.contains(declaration.containingClassName) == false) {
                    continue
                }
                if (declaration.isStatic == isStaticReference) {
                    return
                }
            }
            val warning = when {
                isStaticReference -> "Instance method called from static reference"
                else -> "Static method called from class instance"
            }
            //Logger.getLogger("ObjJMethCallAnnot").log(Level.INFO, "Method call is static? $isStaticReference; For call target: $callTarget")
            for (selector in methodCall.selectorList) {
                problemsHolder.registerProblem(selector, warning)
            }
        }
    }
}