package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.ObjJFrameworkFileName
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJImportElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.utils.ObjJFrameworkUtils
import cappuccino.ide.intellij.plugin.utils.enclosingFrameworkName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.FileContentUtil

class ObjJFrameworkFileNameReference(element:ObjJFrameworkFileName)
    : PsiPolyVariantReferenceBase<ObjJFrameworkFileName>(element, TextRange(0, element.textLength))
{
    private val fileName = element.text
    private val frameworkName = element.getParentOfType(ObjJImportElement::class.java)?.frameworkNameString

    private val project:Project get() = myElement.project

    override fun isReferenceTo(element: PsiElement): Boolean {
        return (element is PsiFile) && element.name == fileName && frameworkName != null && element.hasFramework(frameworkName)
    }

    override fun multiResolve(p0: Boolean): Array<ResolveResult> {
        if (frameworkName == null) {
            return emptyArray()
        }
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

    override fun handleElementRename(newElementName: String): PsiElement {
        val out = ObjJPsiImplUtil.setName(element, newElementName)
        FileContentUtil.reparseFiles(listOf(element.containingFile.virtualFile))
        return out
    }
}

internal fun PsiElement.hasFramework(frameworkName:String?) : Boolean {
    return enclosingFrameworkName == frameworkName
}