package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.psi.ObjJFileNameAsImportString
import cappuccino.ide.intellij.plugin.utils.substringFromEnd
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

class ObjJFileNameAsStringLiteralReference(element:ObjJFileNameAsImportString)
    : PsiPolyVariantReferenceBase<ObjJFileNameAsImportString>(element, TextRange(0, element.stringLiteral.textLength - 2))
{

    private val fileName = element.stringLiteral.stringValue

    override fun isReferenceTo(element: PsiElement): Boolean {
        return (element is PsiFile) && element.name == fileName
    }

    override fun multiResolve(p0: Boolean): Array<ResolveResult> {
        var directory:PsiDirectory? = element.containingFile.parent ?: return emptyArray()
        while (directory != null) {
            directory.files.forEach {
                if (it.name == fileName || it.name.substringFromEnd(0, 2) == fileName) {
                    return PsiElementResolveResult.createResults(it)
                }
            }
            directory = directory.parentDirectory
        }
        val rawResult = FilenameIndex.getFilesByName(myElement.project, fileName, GlobalSearchScope.EMPTY_SCOPE).mapNotNull {
            it.firstChild
        }
        return PsiElementResolveResult.createResults(rawResult)
    }

}