package cappuccino.ide.intellij.plugin.jsdef

import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasTreeStructureElement
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.psi.FileViewProvider

class JsTypeDefFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, JsTypeDefLanguage.INSTANCE), JsTypeDefCompositeElement {

}