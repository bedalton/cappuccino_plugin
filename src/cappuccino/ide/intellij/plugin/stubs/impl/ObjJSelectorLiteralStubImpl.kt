package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.psi.impl.ObjJSelectorLiteralImpl
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJSelectorLiteralStub
import cappuccino.ide.intellij.plugin.stubs.stucts.ObjJSelectorStruct
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import cappuccino.ide.intellij.plugin.utils.ArrayUtils
import com.intellij.psi.stubs.StubElement

class ObjJSelectorLiteralStubImpl(
        parent: StubElement<*>,
        override val containingClassName: String,
        override val selectorStrings: List<String>,
        private val shouldResolve: Boolean,
        override val selectorStructs: List<ObjJSelectorStruct>
) : ObjJStubBaseImpl<ObjJSelectorLiteralImpl>(parent, ObjJStubTypes.SELECTOR_LITERAL), ObjJSelectorLiteralStub {
    override val selectorString: String = ArrayUtils.join(selectorStrings, ObjJMethodPsiUtils.SELECTOR_SYMBOL, true)
    override val isPrivate: Boolean = false
    override val ignored:Boolean = false

    override val parameterTypes: List<String>
        get() = emptyList()

    override val isRequired: Boolean
        get() = false

    override val explicitReturnType: String
        get() = ObjJClassType.UNDEF_CLASS_NAME

    override val isStatic: Boolean
        get() = false

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }

}
