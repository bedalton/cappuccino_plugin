package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.psi.ObjJFrameworkReference
import cappuccino.ide.intellij.plugin.psi.ObjJStringLiteral
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptySibling
import cappuccino.ide.intellij.plugin.psi.utils.getSelfOrParentOfType
import cappuccino.ide.intellij.plugin.psi.utils.isOrHasParentOfType
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import cappuccino.ide.intellij.plugin.utils.ObjJImportUtils
import cappuccino.ide.intellij.plugin.utils.enclosingFrameworkName
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement

object ObjJImportContributor {

    private fun getFrameworkName(element: PsiElement):String? {
        return element.getSelfOrParentOfType(ObjJFrameworkReference::class.java)?.frameworkName?.text
    }

    fun addImportCompletions(resultSet: CompletionResultSet, element:PsiElement) {
        val project = element.project
        val prevSiblingText = element.getPreviousNonEmptySibling(false)?.text
        if (prevSiblingText == "<") {
            ObjJImportUtils.frameworkNames(project).forEach { frameworkName ->
                resultSet.addElement(LookupElementBuilder.create(frameworkName).withInsertHandler(ObjJFrameworkNameInsertHandler))
            }
        } else if (prevSiblingText == "/") {
            val frameworkName = getFrameworkName(element)
            if (frameworkName != null) {
                ObjJImportUtils.getFrameworkFileNames(project, frameworkName).forEach {fileName ->
                    resultSet.addElement(LookupElementBuilder.create(fileName).withInsertHandler(ObjJFrameworkFileNameInsertHandler))
                }

            } else {
                ObjJImportUtils.getFileNamesInDirectory(element.containingFile.containingDirectory, true).forEach {fileName ->
                    resultSet.addElement(LookupElementBuilder.create(fileName).withInsertHandler(ObjJFrameworkNameInsertHandler))
                }
            }
            return
        } else {
            val stringLiteral = element.getSelfOrParentOfType(ObjJStringLiteral::class.java) ?: return
            val frameworkName = stringLiteral.enclosingFrameworkName
            ObjJImportUtils.getFrameworkFileNames(project, frameworkName).forEach {fileName ->
                resultSet.addElement(LookupElementBuilder.create(fileName).withInsertHandler(ObjJFrameworkFileNameInStringInsertHandler))
            }
            ObjJImportUtils.getFileNamesInDirectory(element.containingFile.containingDirectory, true).forEach {fileName ->
                resultSet.addElement(LookupElementBuilder.create(fileName).withInsertHandler(ObjJFrameworkNameInsertHandler))
            }
        }
    }

}

/**
 * Handler for completion insertion of function names
 */
object ObjJFrameworkNameInsertHandler : InsertHandler<LookupElement> {
    /**
     * Actually handle the insertion
     */
    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        if (!EditorUtil.isTextAtOffset(insertionContext, "/")) {
            EditorUtil.insertText(insertionContext, "/", true)
        }
    }
}


/**
 * Handler for completion insertion of function names
 */
object ObjJFrameworkFileNameInsertHandler : InsertHandler<LookupElement> {
    /**
     * Actually handle the insertion
     */
    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        if (!EditorUtil.isTextAtOffset(insertionContext, ">")) {
            EditorUtil.insertText(insertionContext, ">", true)
        }
    }
}


/**
 * Handler for completion insertion of function names
 */
object ObjJFrameworkFileNameInStringInsertHandler : InsertHandler<LookupElement> {
    /**
     * Actually handle the insertion
     */
    override fun handleInsert(insertionContext: InsertionContext, lookupElement: LookupElement) {
        val stringLiteral = lookupElement.psiElement as? ObjJStringLiteral ?: return
        val quote = stringLiteral.text.toCharArray().firstOrNull() ?: return
        if (stringLiteral.text.toCharArray().lastOrNull() == quote)
            return
        EditorUtil.insertText(insertionContext, quote.toString(), false)
    }
}