package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.ObjJFileNameAsImportString
import cappuccino.ide.intellij.plugin.utils.EMPTY_FRAMEWORK_NAME
import cappuccino.ide.intellij.plugin.utils.ObjJFrameworkUtils
import cappuccino.ide.intellij.plugin.utils.enclosingFrameworkName
import cappuccino.ide.intellij.plugin.utils.substringFromEnd
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope

class ObjJFileNameAsStringLiteralReference(element:ObjJFileNameAsImportString)
    : PsiPolyVariantReferenceBase<ObjJFileNameAsImportString>(element, getRange(element))
{

    private val fileName = element.stringLiteral.stringValue.afterSlash()
    private val frameworkName = element.enclosingFrameworkName
    private val project:Project get() = element.project

    override fun isReferenceTo(element: PsiElement): Boolean {
        return (element is PsiFile) && element.name == fileName && element.hasFramework(frameworkName)
    }

    override fun multiResolve(p0: Boolean): Array<ResolveResult> {
        val files = FilenameIndex.getFilesByName(myElement.project, fileName, GlobalSearchScope.everythingScope(project))
                .filter { parentIsFramework(it, frameworkName) }
        return PsiElementResolveResult.createResults(files)
    }

    private fun parentIsFramework(file:PsiFile, frameworkName:String?) : Boolean {
        if (frameworkName == null) {
            return false
        }
        return ((file as? ObjJFile)?.frameworkName ?: ObjJFrameworkUtils.getEnclosingFrameworkName(file)) == frameworkName
    }

}

private fun getRange(element:ObjJFileNameAsImportString) : TextRange {
    if (element.textLength < 2)
        return TextRange.EMPTY_RANGE
    val offset = if (element.text.endsWith("'") || element.text.endsWith("\"")) 1 else 0
    return TextRange(1, element.textLength - offset)
}

private fun String.afterSlash():String {
    val lastPos = this.lastIndexOf("/")
    if (lastPos < 0)
        return this
    return this.substring(lastPos)
}