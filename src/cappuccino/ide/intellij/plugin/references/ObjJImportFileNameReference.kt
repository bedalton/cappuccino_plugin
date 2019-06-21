package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.psi.ObjJFrameworkFileName
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJImportStatement
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

class ObjJImportFileNameReference(element:ObjJFrameworkFileName)
    : PsiPolyVariantReferenceBase<ObjJFrameworkFileName>(element, TextRange(0, element.textLength))
{

    private val fileName = element.text
    private val frameworkName = element.getParentOfType(ObjJImportStatement::class.java)?.frameworkNameString

    override fun isReferenceTo(element: PsiElement): Boolean {
        return (element is PsiFile) && element.name == fileName
    }

    override fun multiResolve(p0: Boolean): Array<ResolveResult> {
        val files = FilenameIndex.getFilesByName(myElement.project, fileName, GlobalSearchScope.EMPTY_SCOPE)
                .filter { parentIsFramework(it) }
        return PsiElementResolveResult.createResults(files)
    }

    private fun parentIsFramework(file:PsiFile) : Boolean {
        if (frameworkName == null) {
            return false
        }
        val project = myElement.project
        var directory = file.parent
        var plist:PsiFile? = null
        val frameworkRegex = frameworkSearchRegex(frameworkName)
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

private fun PsiFile.isForFramework(regex:Regex) : Boolean {
    if (this.name.toLowerCase() != "Info.plist")
        return false
    return regex.containsMatchIn(text)
}

fun frameworkSearchRegex(frameworkName:String) = """<key>CPBundleName</key>\s*<string>\s*$frameworkName\s*</string>""".toRegex()