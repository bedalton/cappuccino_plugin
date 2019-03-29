package cappuccino.ide.intellij.plugin.inspections

import cappuccino.ide.intellij.plugin.fixes.*
import cappuccino.ide.intellij.plugin.lang.ObjJBundle
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.utils.ObjJClassTypePsiUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import java.util.logging.Logger

/**
 * Inspection to flag invalid classes named in method header parameters
 */
class ObjJInvalidMethodParameterClassTypeInspection : LocalInspectionTool() {

    override fun getShortName(): String {
        return "InvalidMethodParameterClassType"
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ObjJVisitor() {
            override fun visitMethodDeclarationSelector(selector: ObjJMethodDeclarationSelector) {
                super.visitMethodDeclarationSelector(selector)
                validateMethodDeclarationParameterClass(selector, holder)
            }
            override fun visitFirstMethodDeclarationSelector(selector: ObjJFirstMethodDeclarationSelector) {
                super.visitMethodDeclarationSelector(selector)
                validateMethodDeclarationParameterClass(selector, holder)
            }
        }
    }

    companion object {

        val LOGGER:Logger by lazy {
            Logger.getLogger("#${ObjJInvalidMethodParameterClassTypeInspection::class.java.canonicalName}")
        }

        fun validateMethodDeclarationParameterClass(selector:ObjJMethodDeclarationSelector, problemsHolder: ProblemsHolder) {
            val className = getClassNameParam(selector)
                    ?: return
            val isValid = ObjJClassTypePsiUtil.isValidClass(className)
                    ?: return
            if (ObjJPluginSettings.isIgnoredClassName(className.text)) {
                problemsHolder.registerProblem(className, ObjJBundle.message("objective-j.inspections.class-type-inspection.is-ignored.message", className.text), ProblemHighlightType.INFORMATION, ObjJAlterIgnoredClassNames(className.text, false))
                return
            }
            if (isValid)
                return
            val classNameText = className.text?.replace("(Ref|Pointer)$".toRegex(), "") ?: return
            val classNameEndedInPointerOrRef = classNameText != className.text
            when {
                classNameEndedInPointerOrRef -> {
                    val isValidWithoutRefOrPointer = ObjJClassTypePsiUtil.isValidClass(classNameText, className.project) ?: return
                    if (isValidWithoutRefOrPointer && ObjJPluginSettings.ignoreMissingClassesWhenSuffixedWithRefOrPointer) {
                        return
                    }
                    registerIfEndsInRefOrPointer(className, problemsHolder)
                }
                classNameText.startsWith("CG") -> registerIfEndsInCG(className, problemsHolder)
                else -> registerDefault(className, problemsHolder)
            }
        }

        private fun getClassNameParam(selector:ObjJMethodDeclarationSelector) : ObjJClassName? {
            val formalVariableType = selector.formalVariableType
                    ?: return null
            return formalVariableType.varTypeId?.className ?: formalVariableType.className
        }

        private fun registerIfEndsInCG(className: ObjJClassName, problemsHolder: ProblemsHolder) {
            problemsHolder.registerProblem(className, ObjJBundle.message("objective-j.annotator-messages.implementation-annotator.instance-var.possibly-undec-class.message", className.text),
                    ProblemHighlightType.WEAK_WARNING,
                    ObjJAddSuppressInspectionForScope(className, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_CLASS, ObjJSuppressInspectionScope.METHOD),
                    ObjJAddSuppressInspectionForScope(className, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_CLASS, ObjJSuppressInspectionScope.CLASS),
                    ObjJAddSuppressInspectionForScope(className, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_CLASS, ObjJSuppressInspectionScope.FILE),
                    ObjJAlterIgnoredClassNames(className.text, addToIgnored = true))
        }

        private fun registerIfEndsInRefOrPointer(className:ObjJClassName, problemsHolder: ProblemsHolder) {
            if (ObjJPluginSettings.ignoreMissingClassesWhenSuffixedWithRefOrPointer) {
                registerIfEndsInRefOrPointerAndSettingIsTrue(className, problemsHolder)
            } else {
                registerIfEndsInRefOrPointerAndSettingIsFalse(className, problemsHolder)
            }
            return
        }

        private fun registerIfEndsInRefOrPointerAndSettingIsTrue(className:ObjJClassName, problemsHolder: ProblemsHolder) {
            problemsHolder.registerProblem(
                    className,
                    ObjJBundle.message("objective-j.inspections.class-type-inspection.is-ignoring-ref-and-pointer.message"),
                    ProblemHighlightType.INFORMATION,
                    ObjJAlterIgnoreClassNamesWithSuffixRefOrPointer(false),
                    ObjJAlterIgnoredClassNames(className.text, addToIgnored = true)
            )
        }

        private fun registerIfEndsInRefOrPointerAndSettingIsFalse(className:ObjJClassName, problemsHolder: ProblemsHolder) {
            problemsHolder.registerProblem(
                    className,
                    ObjJBundle.message("objective-j.annotator-messages.implementation-annotator.instance-var.possibly-undec-class.message", className.text),
                    ObjJAlterIgnoreClassNamesWithSuffixRefOrPointer(true),
                    ObjJAddSuppressInspectionForScope(className, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_CLASS, ObjJSuppressInspectionScope.METHOD),
                    ObjJAddSuppressInspectionForScope(className, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_CLASS, ObjJSuppressInspectionScope.CLASS),
                    ObjJAddSuppressInspectionForScope(className, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_CLASS, ObjJSuppressInspectionScope.FILE),
                    ObjJAlterIgnoredClassNames(className.text, addToIgnored = true)
            )
        }

        private fun registerDefault(className: ObjJClassName, problemsHolder: ProblemsHolder) {
            problemsHolder.registerProblem(className, ObjJBundle.message("objective-j.annotator-messages.implementation-annotator.instance-var.possibly-undec-class.message", className.text),
                    ObjJAddSuppressInspectionForScope(className, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_CLASS, ObjJSuppressInspectionScope.METHOD),
                    ObjJAddSuppressInspectionForScope(className, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_CLASS, ObjJSuppressInspectionScope.CLASS),
                    ObjJAddSuppressInspectionForScope(className, ObjJSuppressInspectionFlags.IGNORE_UNDECLARED_CLASS, ObjJSuppressInspectionScope.FILE),
                    ObjJAlterIgnoredClassNames(className.text, addToIgnored = true))
        }
    }
}