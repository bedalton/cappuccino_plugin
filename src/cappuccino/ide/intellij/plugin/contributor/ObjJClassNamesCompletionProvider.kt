package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.contributor.handlers.ObjJClassNameInsertHandler
import cappuccino.ide.intellij.plugin.contributor.utils.ObjJCompletionElementProviderUtil
import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.elementType
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptySibling
import cappuccino.ide.intellij.plugin.psi.utils.hasParentOfType
import cappuccino.ide.intellij.plugin.psi.utils.thisOrParentAs
import cappuccino.ide.intellij.plugin.references.NoIndex
import cappuccino.ide.intellij.plugin.references.ObjJIgnoreEvaluatorUtil
import cappuccino.ide.intellij.plugin.references.ObjJSuppressInspectionFlags
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement

object ObjJClassNamesCompletionProvider {

    /**
     * Get all defined class names as completions
     */
    internal fun getClassNameCompletions(resultSet: CompletionResultSet, element: PsiElement?) {
        if (element == null) {
            return
        }

        // If is first item in array, there is a chance that this array will truly
        // become a method call, no way to be sure until a comma or selector is written
        val isFirstItemInArray = isFirstItemInArray(element)

        // If in method header, fill in with protocols and classes
        val inMethodHeader = element.parent is ObjJClassDeclarationElement<*> && element.getPreviousNonEmptySibling(true).elementType in listOf(ObjJTypes.ObjJ_COLON, ObjJTypes.ObjJ_OPEN_PAREN)

        // Add protocols if allowed
        if (shouldAddProtocolNameCompletions(element) || inMethodHeader || isFirstItemInArray) {
            ObjJProtocolDeclarationsIndex.instance.getAllKeys(element.project).forEach {
                resultSet.addElement(LookupElementBuilder.create(it).withInsertHandler(ObjJClassNameInsertHandler))
            }
        }

        // Append primitive var types if necessary
        if (shouldAddPrimitiveTypes(element) || inMethodHeader) {
            ObjJClassType.ADDITIONAL_PREDEFINED_CLASSES.forEach {
                resultSet.addElement(LookupElementBuilder.create(it).withInsertHandler(ObjJClassNameInsertHandler))
            }
        }

        // Append implementation declaration names if in correct context
        if (shouldAddImplementationClassNameCompletions(element) || inMethodHeader || isFirstItemInArray) {
            addImplementationClassNameElements(element, resultSet)
            ObjJCompletionElementProviderUtil.addCompletionElementsSimple(resultSet, ObjJPluginSettings.ignoredClassNames())
        }
    }

    private fun isFirstItemInArray(element: PsiElement): Boolean {
        val expression = element.thisOrParentAs(ObjJExpr::class.java) ?: return false
        val arrayLiteralParent = expression.parent as? ObjJArrayLiteral ?: return false
        return arrayLiteralParent.getChildrenOfType(ObjJExpr::class.java).size == 1
    }

    /**
     * Evaluates whether protocol name completions should be added to completion result
     */
    private fun shouldAddProtocolNameCompletions(element: PsiElement): Boolean {
        return element.hasParentOfType(ObjJProtocolLiteral::class.java) ||
                element.hasParentOfType(ObjJInheritedProtocolList::class.java) ||
                element.hasParentOfType(ObjJFormalVariableType::class.java) ||
                element.elementType in ObjJTokenSets.COMMENTS
    }

    /**
     * Evaluates whether or not primitive var types should be added to completion result
     */
    private fun shouldAddPrimitiveTypes(element: PsiElement): Boolean {
        return element.hasParentOfType(ObjJFormalVariableType::class.java)
    }

    /**
     * Evaluates whether implementation class names should be added to completion result
     */
    private fun shouldAddImplementationClassNameCompletions(element: PsiElement): Boolean {
        return element.hasParentOfType(ObjJCallTarget::class.java) ||
                element.hasParentOfType(ObjJFormalVariableType::class.java) ||
                element.elementType in ObjJTokenSets.COMMENTS ||
                element.parent?.elementType in ObjJTokenSets.COMMENTS

    }

    /**
     * Add implementation class names to result set
     */
    private fun addImplementationClassNameElements(element: PsiElement, resultSet: CompletionResultSet) {
        /*
        val thisParts = element.text.split("[A-Z]".toRegex()).filter { it.length < 2 }
        ObjJImplementationDeclarationsIndex.instance.getAll(element.project).forEach { implementationDeclaration ->
            val classParts = implementationDeclaration.getClassNameString().split("[A-Z]".toRegex()).filter { it.length < 2 }
            if (!classParts.startsWithAny(thisParts)) {
                return@forEach
            }
            if (isIgnoredImplementationDeclaration(element, implementationDeclaration)) {
                return@forEach
            }

            resultSet.addElement(LookupElementBuilder.create(implementationDeclaration.getClassNameString()).withInsertHandler(ObjJClassNameInsertHandler))
        }*/
        ObjJImplementationDeclarationsIndex.instance.getAllKeys(element.project).filterNot {
            ObjJPluginSettings.ignoreUnderscoredClasses && it.startsWith("_")
        }.toSet().forEach {
            resultSet.addElement(LookupElementBuilder.create(it).withInsertHandler(ObjJClassNameInsertHandler))
        }
    }

    /**
     * Determines whether or not an implementation declaration is ignored
     */
    private fun isIgnoredImplementationDeclaration(element: PsiElement, declaration: ObjJImplementationDeclaration): Boolean {
        return ObjJIgnoreEvaluatorUtil.isIgnored(declaration) ||
                ObjJIgnoreEvaluatorUtil.shouldIgnoreUnderscore(element) ||
                ObjJIgnoreEvaluatorUtil.isIgnored(element, ObjJSuppressInspectionFlags.IGNORE_CLASS) ||
                ObjJIgnoreEvaluatorUtil.noIndex(declaration, NoIndex.CLASS) ||
                ObjJIgnoreEvaluatorUtil.noIndex(declaration, NoIndex.ANY)
    }

    /**
     * Add protocol name completions
     */
    internal fun addProtocolNameCompletionElements(resultSet: CompletionResultSet, element: PsiElement, queryString: String) {
        val results = ObjJProtocolDeclarationsIndex.instance.getKeysByPattern("$queryString(.+)", element.project) as MutableList<String>
        ObjJCompletionElementProviderUtil.addCompletionElementsSimple(resultSet, results)

    }



}