package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.psi.types.ObjJTokenSets
import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler

class ObjJSimpleQuoteHandler : SimpleTokenSetQuoteHandler(ObjJTokenSets.QUOTE_CHARS)