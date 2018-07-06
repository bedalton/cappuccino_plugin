package cappuccino.ide.intellij.plugin.lang

import com.intellij.openapi.util.Condition
import com.intellij.openapi.vfs.VirtualFile

class ObjJProblemFileHighlighterFilter : Condition<VirtualFile> {
    override fun value(virtualFile: VirtualFile): Boolean {
        val fileType = virtualFile.fileType
        return fileType === ObjJFileType.INSTANCE
    }
}