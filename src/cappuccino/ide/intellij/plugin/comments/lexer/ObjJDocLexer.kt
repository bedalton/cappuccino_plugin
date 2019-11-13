package cappuccino.ide.intellij.plugin.comments.lexer

import cappuccino.ide.intellij.plugin.comments._ObjJCommentLexer
import com.intellij.lexer.FlexAdapter

open class ObjJDocLexer : FlexAdapter(_ObjJCommentLexer())
