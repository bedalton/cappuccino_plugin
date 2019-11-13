package cappuccino.ide.intellij.plugin.comments.lexer

import cappuccino.ide.intellij.plugin.comments.ObjJDocCommentImpl
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.parser.ObjJCommentParser
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilderFactory
import com.intellij.psi.tree.ILazyParseableElementType


var OBJJ_DOC_COMMENT: ILazyParseableElementType = object : ILazyParseableElementType("ObjJ_DOC_COMMENT", ObjJLanguage.instance) {

    override fun parseContents(chameleon: ASTNode): ASTNode {
        val parentElement = chameleon.treeParent.psi
        val project = parentElement.project
        val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, ObjJDocLexer(), language,
                chameleon.text)
        val parser = ObjJCommentParser()
        return parser.parse(this, builder).getFirstChildNode()
    }

    override fun createNode(text: CharSequence): ASTNode {
        return ObjJDocCommentImpl(text)
    }
}