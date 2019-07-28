package cappuccino.ide.intellij.plugin.refactoring

import cappuccino.ide.intellij.plugin.indices.ObjJClassAndSelectorMethodIndex
import cappuccino.ide.intellij.plugin.indices.ObjJClassInheritanceIndex
import cappuccino.ide.intellij.plugin.inference.createTag
import cappuccino.ide.intellij.plugin.inference.inferCallTargetType
import cappuccino.ide.intellij.plugin.inference.toClassList
import cappuccino.ide.intellij.plugin.inference.withoutAnyType
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasMethodSelector
import cappuccino.ide.intellij.plugin.psi.utils.elementType
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import cappuccino.ide.intellij.plugin.utils.enclosingFrameworkName
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.refactoring.rename.naming.AutomaticRenamer
import com.intellij.refactoring.rename.naming.AutomaticRenamerFactory
import com.intellij.usageView.UsageInfo
import javax.naming.OperationNotSupportedException

class ObjJSelectorAutomaticRenamerFactory : AutomaticRenamerFactory {

    var allowRename = ObjJPluginSettings.experimental_allowSelectorRename

    override fun isEnabled(): Boolean {
        return allowRename
    }

    override fun setEnabled(enabled: Boolean) {
        allowRename = enabled
    }

    override fun isApplicable(element: PsiElement): Boolean {
        return element is ObjJSelector
    }

    override fun getOptionName(): String? {
        return "Selector rename is experimental, dangerous, and prone to errors. Rename uses brute force method to rename all matching selectors, regardless of containing class. Continue with rename?"
    }

    override fun createRenamer(element: PsiElement?, newName: String?, usages: MutableCollection<UsageInfo>?): AutomaticRenamer {
        val selector = element as ObjJSelector
        val usagesOut = if (usages.isNullOrEmpty() || newName.isNullOrBlank() || !allowRename)
            mutableListOf()
        else
            usages
        return ObjJSelectorRenamer(selector, usagesOut, newName, ObjJPluginSettings.experimental_allowFrameworkSelectorRename)
    }
}


internal class ObjJSelectorRenamer(val selector: ObjJSelector, usages: MutableCollection<UsageInfo>, newName: String?, allowRenameOutsideOfFramework: Boolean) : AutomaticRenamer() {

    init {
        val tag = createTag()
        val frameworkName = selector.enclosingFrameworkName
        val selectorString = selector.getParentOfType(ObjJHasMethodSelector::class.java)?.selectorString
                ?: throw UnsupportedOperationException("Cannot rename selector out of expected context: "+ selector.parent?.elementType)
        val selectorDirectTypes = getTargetClass(selector, tag)
        val allPossibleClassTypes = if (selectorDirectTypes != null && selectorDirectTypes.isNotEmpty())
            getAllPossibleClassTypes(selector.project, selectorDirectTypes, selectorString)
        else
            null

        val selectorsWithUnknownTypes = mutableListOf<ObjJSelector>()
        val selectorsWithMatchingTypes = mutableListOf<ObjJSelector>()
        getSelectorsCalledFromUnknownClass(
                usages = usages,
                tag = tag,
                targetClasses = allPossibleClassTypes,
                hasUnknownCallingClass = selectorsWithUnknownTypes,
                matchesClasses = selectorsWithMatchingTypes
        )
        var namedElements: List<PsiNamedElement> = if (selectorsWithUnknownTypes.isNotEmpty()) {
            usages.mapNotNull { it.element as? PsiNamedElement }
        } else {
            selectorsWithMatchingTypes
        }

        if (!allowRenameOutsideOfFramework) {
            namedElements = namedElements.filter {
                it.enclosingFrameworkName == frameworkName
            }
        }
        myElements.addAll(namedElements)
        suggestAllNames(selector.name, newName)
    }

    private fun getTargetClass(selector: ObjJSelector, tag: Long): Set<String>? {
        val parent = selector.getParentOfType(ObjJHasMethodSelector::class.java)!!
        if (parent is ObjJSelectorLiteral)
            return null
        if (parent is ObjJMethodCall) {
            return inferCallTargetType(parent.callTarget, tag)?.toClassList(null)
        }
        return setOf(parent.containingClassName)
    }

    private fun getAllPossibleClassTypes(project: Project, baseClasses: Set<String>, selectorString:String): Set<String> {
        val allSuperClasses = baseClasses.flatMap {
            ObjJInheritanceUtil.getAllInheritedClasses(it, project, true).filter {
                val key = ObjJClassAndSelectorMethodIndex.getClassMethodKey(it, selectorString)
                ObjJClassAndSelectorMethodIndex.instance.containsKey(key, project)
            }
        }
        val allChildClasses = baseClasses.flatMap {
            ObjJClassInheritanceIndex.instance.getChildClassesAsStrings(it, project)
        }
        return (baseClasses + allSuperClasses + allChildClasses).toSet()
    }

    private fun getSelectorsCalledFromUnknownClass(usages: MutableCollection<UsageInfo>?, tag: Long, targetClasses:Set<String>?, hasUnknownCallingClass:MutableList<ObjJSelector>, matchesClasses:MutableList<ObjJSelector>) {
        for (usage in usages.orEmpty()) {
            // Get selector or continue
            val selector = usage.element as? ObjJSelector ?: continue

            // If parent is not method call, it cannot have an unknown selector
            val parentMethodCall = selector.getParentOfType(ObjJMethodCall::class.java)
            if (parentMethodCall != null) {
                // Resolve call target type
                val callTarget = parentMethodCall.callTarget
                val resolvedTypes = inferCallTargetType(callTarget, tag)?.toClassList(null)?.withoutAnyType()
                if (targetClasses == null || targetClasses.isEmpty())
                    matchesClasses.add(selector)
                if (resolvedTypes == null || resolvedTypes.isEmpty()) {
                    hasUnknownCallingClass.add(selector)
                    continue
                }
                val overlap = targetClasses?.intersect(resolvedTypes).orEmpty()
                if (overlap.isNotEmpty())
                    matchesClasses.add(selector)
                else
                    hasUnknownCallingClass.add(selector)
            }
        }
    }

    override fun getDialogDescription(): String {
        return "Rename selector using experimental and unstable, brute force renamer"
    }

    override fun getDialogTitle(): String {
        return "Rename Selector"
    }

    override fun entityName(): String {
        return "Selector"
    }


}