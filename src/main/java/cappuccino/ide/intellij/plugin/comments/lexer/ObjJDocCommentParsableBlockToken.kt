package cappuccino.ide.intellij.plugin.comments.lexer

import cappuccino.ide.intellij.plugin.comments.parser.ObjJDocCommentParser
import cappuccino.ide.intellij.plugin.comments.psi.impl.ObjJDocCommentParsableBlockImpl
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilderFactory
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.tree.ILazyParseableElementType

object ObjJDocCommentParsableBlockToken {
    @JvmStatic
    val OBJJ_DOC_COMMENT_PARSABLE_BLOCK: ILazyParseableElementType = object : ILazyParseableElementType("ObjJ_DOC_COMMENT_PARSABLE_BLOCK", ObjJLanguage.instance) {
        override fun parseContents(chameleon: ASTNode): ASTNode {
            val project = chameleon.psi.project
            val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, ObjJDocCommentLexer(), language,
                    chameleon.text)
            val parser = ObjJDocCommentParser()
            return parser.parse(this, builder).firstChildNode
        }

        override fun createNode(text: CharSequence?): ASTNode? {
            return ObjJDocCommentParsableBlockImpl(text)
        }
    }
}