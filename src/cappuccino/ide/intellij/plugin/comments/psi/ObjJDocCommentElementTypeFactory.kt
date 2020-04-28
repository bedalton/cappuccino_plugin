package cappuccino.ide.intellij.plugin.comments.psi

import cappuccino.ide.intellij.plugin.comments.psi.stubs.ObjJDocCommentStubElementTypes
import cappuccino.ide.intellij.plugin.comments.psi.stubs.ObjJ_DOC_COMMENT_TAG_LINE
import com.intellij.psi.tree.IElementType

object ObjJDocCommentElementTypeFactory {
    @JvmStatic
    fun factory(name: String): IElementType {
        return when (name) {
            "ObjJDocComment_COMMENT" -> ObjJDocCommentStubElementTypes.ObjJ_DOC_COMMENT_COMMENT_STUB_TYPE
            "ObjJDocComment_TAG_LINE" -> ObjJ_DOC_COMMENT_TAG_LINE
            else -> throw RuntimeException("Failed to find element type in factory for type <$name>")
        }
    }
}