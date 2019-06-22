package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.lang.ObjJFileType
import cappuccino.ide.intellij.plugin.psi.ObjJFrameworkReference
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptySibling
import cappuccino.ide.intellij.plugin.psi.utils.getSelfOrParentOfType
import cappuccino.ide.intellij.plugin.references.frameworkSearchRegex
import cappuccino.ide.intellij.plugin.utils.EditorUtil
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import java.util.regex.Pattern

object ObjJImportContributor {
    private val bundleRegex = Pattern.compile("<key>CPBundleName</key>\\s*<string>([^<]+?)</string>")

    private fun frameworkNames(project:Project):List<String> {
        return FilenameIndex.getFilesByName(project, "Info.plist", GlobalSearchScope.everythingScope(project)).mapNotNull { file ->
            val matcher = bundleRegex.matcher(file.text)
            if (matcher.find()) {
                val match = matcher.group(1)
                match
            } else {
                null
            }
        }
    }

    private fun getFrameworkDirectory(project: Project, frameworkName:String) : PsiDirectory? {
        val searchRegex = frameworkSearchRegex(frameworkName)
        return FilenameIndex.getFilesByName(project, "Info.plist", GlobalSearchScope.everythingScope(project)).firstOrNull { file ->
            searchRegex.containsMatchIn(file.text)
        }?.containingDirectory
    }

    private fun getFileNames(project:Project, frameworkName:String) : List<String> {
        val frameworkDirectory = getFrameworkDirectory(project, frameworkName)
        return getFileNamesInDirectory(frameworkDirectory)
    }

    private fun getFileNamesInDirectory(directory: PsiDirectory?, recursive:Boolean = true) : List<String> {
        if (directory == null) {
            return emptyList()
        }
        val out = mutableListOf<String>()
        directory.files.forEach { file ->
            if (file.isDirectory && recursive) {
                out.addAll(getFileNamesInDirectory(file as PsiDirectory))
            } else if (file.fileType == ObjJFileType.INSTANCE){
                out.add(file.name)
            }
        }
        return out
    }

    private fun getFrameworkName(element:PsiElement):String? {
        return element.getSelfOrParentOfType(ObjJFrameworkReference::class.java)?.frameworkName?.text
    }


    fun addImportCompletions(resultSet: CompletionResultSet, element:PsiElement) {
        val project = element.project
        val prevSiblingText = element.getPreviousNonEmptySibling(false)?.text ?: null
        if (prevSiblingText == "<") {
            frameworkNames(project).forEach { frameworkName ->
                resultSet.addElement(LookupElementBuilder.create(frameworkName).withInsertHandler(ObjJFrameworkNameInsertHandler))
            }
        } else if (prevSiblingText == "/") {
            val frameworkName = getFrameworkName(element)
            if (frameworkName != null) {
                getFileNames(project, frameworkName).forEach {fileName ->
                    resultSet.addElement(LookupElementBuilder.create(fileName).withInsertHandler(ObjJFrameworkFileNameInsertHandler))
                }

            } else {
                getFileNamesInDirectory(element.containingFile.containingDirectory, true).forEach {fileName ->
                    resultSet.addElement(LookupElementBuilder.create(fileName).withInsertHandler(ObjJFrameworkNameInsertHandler))
                }
            }
            return
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