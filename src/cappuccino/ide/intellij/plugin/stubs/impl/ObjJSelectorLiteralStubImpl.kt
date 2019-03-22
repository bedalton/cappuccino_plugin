package cappuccino.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.impl.ObjJSelectorLiteralImpl
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJSelectorLiteralStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils

class ObjJSelectorLiteralStubImpl(parent: StubElement<*>, override val containingClassName: String, override val selectorStrings: List<String>, private val shouldResolve: Boolean) : ObjJStubBaseImpl<ObjJSelectorLiteralImpl>(parent, ObjJStubTypes.SELECTOR_LITERAL), ObjJSelectorLiteralStub {
    override val selectorString: String = ArrayUtils.join(selectorStrings, ObjJMethodPsiUtils.SELECTOR_SYMBOL, true)


    override val ignored:Boolean = false

    override val paramTypes: List<String>
        get() = emptyList()

    override val isRequired: Boolean
        get() = false

    override val returnType: ObjJClassType
        get() = ObjJClassType.UNDEF

    override val returnTypeAsString: String
        get() = returnType.className

    override val isStatic: Boolean
        get() = false

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }

}
