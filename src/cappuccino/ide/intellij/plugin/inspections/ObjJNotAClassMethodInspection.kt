package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.indices.ObjJClassAndSelectorMethodIndex
import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.ObjJMethodCall
import cappuccino.ide.intellij.plugin.psi.ObjJSelector
import cappuccino.ide.intellij.plugin.psi.ObjJVisitor
import cappuccino.ide.intellij.plugin.utils.substringFromEnd
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor

class ObjJNotAClassMethodInspection : LocalInspectionTool() {

    override fun getShortName(): String {
        return ObjJBundle.message("objective-j.inspections.not-a-class-method.shortname")
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {

        return object : ObjJVisitor() {
            val tag = createTag()
            override fun visitMethodCall(methodCall: ObjJMethodCall) {
                val project = methodCall.project
                val selectorString = methodCall.selectorString

                if (selectorString == "respondsToSelector:" || selectorString == "class:" || selectorString == "isa:")
                    return

                val selector = methodCall.selectorString ?: return
                val classes = inferCallTargetType(methodCall.callTarget, tag)?.toClassList(null)?.withoutAnyType()?.map {
                    if (it == "object")
                        "CPObject"
                    else
                        it
                }?.toSet() ?: return
                if (classes.isEmpty())
                    return
                // Get quick result from class constraints
                val quickSelectorResult = classes.flatMap { className ->
                    ObjJClassAndSelectorMethodIndex.instance.getByClassAndSelector(className, selector, project)
                }
                if (quickSelectorResult.isNotEmpty())
                    return
                annotateMethodCall(methodCall, classes, holder)

                //super.visitMethodCall(methodCall)
                /*
                val selectorString = methodCall.selectorString

                if (selectorString == "respondsToSelector:" || selectorString == "class:" || selectorString == "isa:")
                    return
                val onlyVariable = methodCall.callTarget.singleVariableNameElementOrNull
                if (onlyVariable != null) {
                    val classes = onlyVariable.getVariableType(tag)?.toClassList(null)?.withoutAnyType()
                    if (classes.isNullOrEmpty() || classes.all { it in anyTypes })
                        return
                    val selectors = onlyVariable.getMethodSelectors(tag)
                    LOGGER.info("$selectorString !in $selectors for classes: $classes")
                    if (selectors.isNotEmpty()) {
                        if (selectorString !in selectors && selectorString.substringFromEnd(0, 1) !in selectors) {

                            annotateMethodCall(methodCall, classes, holder)
                        }
                        return
                    }
                }
                val callTargetType = inferCallTargetType(methodCall.callTarget, tag)
                        ?: return
                val classes = callTargetType.toClassList(null).withoutAnyType()
                if (classes.isEmpty())
                    return
                if (isValid(methodCall, classes, tag))
                    return
                annotateMethodCall(methodCall, classes, holder)
                */
            }
        }
    }

    private fun isValid(methodCall: ObjJMethodCall, classes:Set<String>, tag:Long) : Boolean {
        val project = methodCall.project
        val selectorString = methodCall.selectorString
        if (selectorString in methodCall.callTarget.singleVariableNameElementOrNull?.getMethodSelectors(tag).orEmpty())
            return true
        return classes.any {
            ObjJClassAndSelectorMethodIndex.instance.containsKey(it, selectorString, project) ||
            ObjJClassAndSelectorMethodIndex.instance.containsKey(it, selectorString.substringFromEnd(0,1), project)
        }
    }

    private fun annotateMethodCall(methodCall: ObjJMethodCall, callTargetType:Set<String>, problemsHolder: ProblemsHolder) {
        val callTarget = "[${callTargetType.joinToString("|")}]"
        val selectorString = methodCall.selectorString
        methodCall.selectorList.forEach { selector ->
            annotateSelectors(selector, selectorString, callTarget, problemsHolder)
        }
    }

    private fun annotateSelectors(selector:ObjJSelector, fullMethodSelector: String, methodTarget:String, problemsHolder: ProblemsHolder) {
        val message = message(fullMethodSelector, methodTarget)
        problemsHolder.registerProblem(selector, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
    }

    companion object {
        private fun message(fullMethodSelector:String, callTarget:String):String {
            return ObjJBundle.message("objective-j.inspections.not-a-class-method.message", fullMethodSelector, callTarget)
        }
    }
}