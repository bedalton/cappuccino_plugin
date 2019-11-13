package cappuccino.ide.intellij.plugin.comments

import cappuccino.ide.intellij.plugin.lang.ObjJFile
import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.parser.ObjJCommentParser
import cappuccino.ide.intellij.plugin.parser.ObjJParserDefinition
import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenType
import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.*
import com.intellij.lang.impl.PsiBuilderImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.impl.source.tree.LazyParseablePsiElement
import com.intellij.psi.tree.ILazyParseableElementType
import com.intellij.lang.PsiParser
import com.intellij.lang.PsiBuilderFactory
import com.intellij.lang.PsiBuilder
import java.lang.reflect.Constructor


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

    override fun getLanguage(): Language {
        return ObjJLanguage.instance
    }

    override val containingObjJFile: ObjJFile?
        get() {
            val file = containingFile
            return file as? ObjJFile
        }

    override fun toString(): String {
        return node.elementType.toString()
    }
}

class ObjJCommentElementType(debug: String, psiClass: Class<out PsiElement>) : IElementType(debug, ObjJLanguage.instance) {
    private val psiFactory: Constructor<out PsiElement>
    init {
        try {
            psiFactory = psiClass.getConstructor(ASTNode::class.java)
        } catch (e: NoSuchMethodException) {
            throw RuntimeException("Must have a constructor with ASTNode")
        }

    }
    fun createPsi(node: ASTNode): PsiElement {
        assert(node.elementType === this)
        try {
            return psiFactory.newInstance(node)
        } catch (e: Exception) {
            throw RuntimeException("Error creating psi element for node", e)
        }

    }
}

class ObjJCommentTokenType(debug: String) : ObjJTokenType(debug)

/**
 * GroovyDoc comment
 */
var OBJJ_DOC_COMMENT: ILazyParseableElementType = object : ILazyParseableElementType("GrDocComment", ObjJLanguage.instance) {

    override fun parseContents(chameleon: ASTNode): ASTNode {
        val parentElement = chameleon.treeParent.psi
        val project = parentElement.project
        val builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, ObjJCommentLexer(), language,
                chameleon.text)
        val parser = ObjJCommentParser()
        return parser.parse(this, builder).getFirstChildNode()
    }

    override fun createNode(text: CharSequence): ASTNode {
        return ObjJDocCommentImpl(text)
    }
}

class ObjJDocCommentImpl(text:CharSequence) : LazyParseablePsiElement(OBJJ_DOC_COMMENT, text)