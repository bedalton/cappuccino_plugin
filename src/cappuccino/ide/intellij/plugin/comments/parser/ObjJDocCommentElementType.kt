package cappuccino.ide.intellij.plugin.comments.parser

import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType

import java.lang.reflect.Constructor

class ObjJDocCommentElementType(debugName: String) : IElementType(debugName, ObjJLanguage.instance)
