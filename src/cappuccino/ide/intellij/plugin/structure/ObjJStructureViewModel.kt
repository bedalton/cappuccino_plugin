package cappuccino.ide.intellij.plugin.structure

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasTreeStructureElement
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiFileUtil
import cappuccino.ide.intellij.plugin.utils.orFalse
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TextEditorBasedStructureViewModel
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import icons.ObjJIcons

class ObjJStructureViewModel internal constructor(psiFile: PsiFile, editor: Editor) : StructureViewModelBase(psiFile, createStructureViewElement(psiFile)), StructureViewModel.ElementInfoProvider {

    override fun getSorters(): Array<Sorter> {
        return arrayOf(Sorter.ALPHA_SORTER)
    }

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean {
        return false
    }

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean {
        return (element as? ObjJStructureViewElement)?.isAlwaysLeaf.orFalse()
    }

    override fun getSuitableClasses(): Array<Class<*>> {
        return arrayOf(
                ObjJGlobalVariableDeclaration::class.java,
                ObjJImplementationDeclaration::class.java,
                ObjJInstanceVariableDeclaration::class.java,
                ObjJMethodDeclaration::class.java,
                ObjJMethodHeader::class.java,
                ObjJProtocolDeclaration::class.java,
                ObjJProtocolScopedMethodBlock::class.java
        )
    }

    companion object {
        private fun createStructureViewElement(psiFile: PsiFile): ObjJStructureViewElement {
            val fileName = ObjJPsiFileUtil.getFileNameSafe(psiFile, "Objective-J File")
            val presentationData = PresentationData(fileName, null, ObjJIcons.DOCUMENT_ICON, null)
            return ObjJStructureViewElement(psiFile as ObjJFile, presentationData, fileName)
        }
    }
}