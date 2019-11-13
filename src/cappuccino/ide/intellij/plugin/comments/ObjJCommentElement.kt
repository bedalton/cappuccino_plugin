package cappuccino.ide.intellij.plugin.comments

import cappuccino.ide.intellij.plugin.comments.lexer.ObjJDocLexer
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.parser.ObjJCommentParser
import cappuccino.ide.intellij.plugin.parser.ObjJParserDefinition
import com.intellij.lang.*
import com.intellij.lang.impl.PsiBuilderImpl
import com.intellij.psi.tree.IElementType
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.impl.source.tree.LazyParseablePsiElement
import com.intellij.psi.tree.ILazyParseableElementType


class ObjJCommentElementType(debug: String) : IElementType(debug, ObjJLanguage.instance)

class ObjJCommentToken(debug: String) : IElementType(debug, ObjJLanguage.instance)

/**
 * GroovyDoc comment
 */
var OBJJ_DOC_COMMENT: ILazyParseableElementType = object : ILazyParseableElementType("GrDocComment") {
    override fun getLanguage(): Language {
        return ObjJLanguage.instance
    }

    override fun parseContents(chameleon: ASTNode): ASTNode {
        val parentElement = chameleon.treeParent.psi
        val project = JavaPsiFacade.getInstance(parentElement.project).project

        val builder = PsiBuilderImpl(project, ObjJParserDefinition(), ObjJDocLexer(), chameleon, chameleon.text)
        val parser = ObjJCommentParser()

        return parser.parse(this, builder).firstChildNode
    }

    override fun createNode(text: CharSequence): ASTNode {
        return ObjJDocCommentImpl(text)
    }
}

class ObjJDocCommentImpl(text:CharSequence) : LazyParseablePsiElement(OBJJ_DOC_COMMENT, text)