package cappuccino.ide.intellij.plugin.comments

import cappuccino.ide.intellij.plugin.comments.lexer.ObjJDocLexer
import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.parser.ObjJCommentParser
import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilderFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LazyParseablePsiElement
import com.intellij.psi.tree.ILazyParseableElementType

interface ObjJCommentElement : PsiElement {

    val containingObjJFile: ObjJFile?
        get() {
            val file = containingFile
            if (file == null) {
               //LOGGER.severe("Cannot get ObjJFile, as containing file is null.")
                return null
            }
            if (file is ObjJFile) {
                return file
            }
           //LOGGER.severe("ObjJFile is actually of type: " + this.containingFile.javaClass.simpleName)
            return null
        }
}


var OBJJ_DOC_COMMENT: ILazyParseableElementType = object : ILazyParseableElementType("GrDocComment", ObjJLanguage.instance) {

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

class ObjJDocCommentImpl(text:CharSequence) : LazyParseablePsiElement(OBJJ_DOC_COMMENT, text)