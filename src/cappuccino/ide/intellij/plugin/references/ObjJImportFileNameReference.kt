package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.psi.ObjJFrameworkFileName
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJImportStatement
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.utils.INFO_PLIST_FILE_NAME_TO_LOWER_CASE
import cappuccino.ide.intellij.plugin.utils.createFrameworkSearchRegex
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

class ObjJImportFileNameReference(element:ObjJFrameworkFileName)
    : PsiPolyVariantReferenceBase<ObjJFrameworkFileName>(element, TextRange(0, element.textLength))
{

    private val fileName = element.text
    private val frameworkName = element.getParentOfType(ObjJImportStatement::class.java)?.frameworkNameString

    private val project:Project get() = myElement.project

    override fun isReferenceTo(element: PsiElement): Boolean {
        return (element is PsiFile) && element.name == fileName && frameworkName != null && element.isForFramework(frameworkName)
    }

    override fun multiResolve(p0: Boolean): Array<ResolveResult> {
        LOGGER.info("Multi-resolve framework file")
        if (frameworkName == null) {
            LOGGER.info("Framework name is null")
            return emptyArray()
        }
        LOGGER.info("Framework name is <$frameworkName>")
        val frameworkRegex = createFrameworkSearchRegex(frameworkName)
        val files = FilenameIndex.getFilesByName(myElement.project, fileName, GlobalSearchScope.everythingScope(project))
                .filter { parentIsFramework(it, frameworkRegex) }
        LOGGER.info("Found <${files.size}> files with import name: <$fileName> and framework: <$frameworkName>")
        return PsiElementResolveResult.createResults(files)
    }

    private fun parentIsFramework(file:PsiFile, frameworkRegex:Regex) : Boolean {
        if (frameworkName == null) {
            return false
        }
        var directory = file.parent
        var plist: PsiFile?
        while (directory != null) {
            plist = directory.findFile("info.plist")
            if (plist != null && plist.isForFramework(frameworkRegex)) {
                return true
            }
            directory = directory.parentDirectory
        }
        return false
    }

}

private fun PsiFile.isForFramework(frameworkName:String) : Boolean {
    val frameworkRegex = createFrameworkSearchRegex(frameworkName)
    return isForFramework(frameworkRegex)
}

private fun PsiFile.isForFramework(regex:Regex) : Boolean {
    if (this.name.toLowerCase() != INFO_PLIST_FILE_NAME_TO_LOWER_CASE)
        return false
    return regex.containsMatchIn(text)
}
