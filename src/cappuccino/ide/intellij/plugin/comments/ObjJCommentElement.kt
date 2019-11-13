package cappuccino.ide.intellij.plugin.comments

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.parser.ObjJCommentParser
import cappuccino.ide.intellij.plugin.parser.ObjJParserDefinition
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.*
import com.intellij.lang.impl.PsiBuilderImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.JavaPsiFacade
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

open class ObjJCommentElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), ObjJCommentElement {

    override val containingObjJFile: ObjJFile?
        get() {
            val file = containingFile
            return file as? ObjJFile
        }

    override fun toString(): String {
        return node.elementType.toString()
    }
}

class ObjJCommentElementType(debug: String) : IElementType(debug, ObjJLanguage.instance)

class ObjJCommentTokenType(debug: String) : IElementType(debug, ObjJLanguage.instance)

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

        val builder = PsiBuilderImpl(project, ObjJParserDefinition(), ObjJCommentLexer(), chameleon, chameleon.text)
        val parser = ObjJCommentParser()

        return parser.parse(this, builder).firstChildNode
    }

    override fun createNode(text: CharSequence): ASTNode {
        return ObjJDocCommentImpl(text)
    }
}

class ObjJDocCommentImpl(text:CharSequence) : LazyParseablePsiElement(OBJJ_DOC_COMMENT, text)