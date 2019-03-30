package cappuccino.ide.intellij.plugin.contributor

import com.intellij.psi.*
import com.intellij.util.ProcessingContext
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.references.ObjJClassNameReference
import cappuccino.ide.intellij.plugin.references.ObjJFunctionNameReference
import cappuccino.ide.intellij.plugin.references.ObjJSelectorReference
import cappuccino.ide.intellij.plugin.references.ObjJVariableReference

import com.intellij.patterns.PlatformPatterns.psiElement

class ObjJReferenceContributor : PsiReferenceContributor() {


    override fun registerReferenceProviders(
            psiReferenceRegistrar: PsiReferenceRegistrar) {
        //Selector
        val selector = psiElement(ObjJSelector::class.java)
        psiReferenceRegistrar.registerReferenceProvider(selector, SelectorReferenceProvider())

        //ClassName
        val classNameCapture = psiElement(ObjJClassName::class.java)
        psiReferenceRegistrar.registerReferenceProvider(classNameCapture, ClassNameReferenceProvider())

        //VariableName
        val variableName = psiElement(ObjJVariableName::class.java)
        psiReferenceRegistrar.registerReferenceProvider(variableName, VariableNameReferenceProvider())

        val functionName = psiElement(ObjJFunctionName::class.java)
        psiReferenceRegistrar.registerReferenceProvider(functionName, FunctionNameReferenceProvider())
    }

    private class SelectorReferenceProvider : PsiReferenceProvider() {

        override fun getReferencesByElement(
                psiElement: PsiElement,
                processingContext: ProcessingContext): Array<PsiReference> {
            if (psiElement !is ObjJSelector) {
                return PsiReference.EMPTY_ARRAY
            }

            val getter: ObjJSelector? = null
            val baseSelector = psiElement.getSelectorString(false)
            var getterString = baseSelector
            val startsWithUnderscore = getterString.startsWith("_")
            if (startsWithUnderscore) {
                getterString = getterString.substring(1)
            }
            if (getterString.startsWith("is")) {
                getterString = getterString.substring(2)
            }
            if (getterString.startsWith("set")) {
                getterString = getterString.substring(3)
            }
            getterString = (if (startsWithUnderscore) "_" else "") + if (getterString.length > 1) getterString.substring(0, 1).toLowerCase() + getterString.substring(1) else getterString
            if (getterString != baseSelector) {
                //getter = ObjJElementFactory.createSelector(selector.getProject(), getterString);
            }
            return if (getter != null) {
                arrayOf(createReference(psiElement), createReference(getter))
            } else arrayOf(createReference(psiElement))
        }

        private fun createReference(selector: ObjJSelector): PsiReference {
            return ObjJSelectorReference(selector)
        }

        override fun acceptsTarget(target: PsiElement): Boolean {
            return target is ObjJSelector
        }
    }


    private class FunctionNameReferenceProvider : PsiReferenceProvider() {

        override fun getReferencesByElement(
                psiElement: PsiElement,
                processingContext: ProcessingContext): Array<PsiReference> {
            return if (psiElement is ObjJFunctionName) arrayOf(createReference(psiElement)) else PsiReference.EMPTY_ARRAY
        }

        private fun createReference(functionName: ObjJFunctionName): PsiReference {
            return ObjJFunctionNameReference(functionName)
        }

        override fun acceptsTarget(target: PsiElement): Boolean {
            return target is ObjJFunctionName
        }
    }


    private class ClassNameReferenceProvider : PsiReferenceProvider() {

        override fun getReferencesByElement(
                psiElement: PsiElement,
                processingContext: ProcessingContext): Array<PsiReference> {
            return if (psiElement is ObjJClassName) arrayOf(createReference(psiElement)) else PsiReference.EMPTY_ARRAY
        }

        private fun createReference(className: ObjJClassName): PsiReference {
            return ObjJClassNameReference(className)
        }

        override fun acceptsTarget(target: PsiElement): Boolean {
            return target is ObjJClassName
        }
    }

    private class VariableNameReferenceProvider : PsiReferenceProvider() {

        override fun getReferencesByElement(
                psiElement: PsiElement,
                processingContext: ProcessingContext): Array<PsiReference> {
            return if (psiElement is ObjJClassName) arrayOf(createReference(psiElement as ObjJVariableName)) else PsiReference.EMPTY_ARRAY
        }

        private fun createReference(variableName: ObjJVariableName): PsiReference {
            return ObjJVariableReference(variableName)
        }

        override fun acceptsTarget(target: PsiElement): Boolean {
            return target is ObjJVariableName && target.getParentOfType( ObjJFunctionCall::class.java) == null
        }
    }

}
