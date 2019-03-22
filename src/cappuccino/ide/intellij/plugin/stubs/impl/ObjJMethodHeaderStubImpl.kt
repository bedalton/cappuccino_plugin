package cappuccino.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.impl.ObjJMethodHeaderImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassTypeName
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils

class ObjJMethodHeaderStubImpl(parent: StubElement<*>?, className: String?, override val isStatic: Boolean, override val selectorStrings: List<String>, override val paramTypes: List<String>, returnType: String?, override val isRequired: Boolean, private val shouldResolve: Boolean, override val ignored:Boolean) : ObjJStubBaseImpl<ObjJMethodHeaderImpl>(parent, ObjJStubTypes.METHOD_HEADER), ObjJMethodHeaderStub {
    override val returnTypeAsString: String = returnType ?: ObjJClassType.UNDETERMINED
    override val containingClassName: String = className ?: ObjJClassType.UNDEF_CLASS_NAME
    override val selectorString: String = ObjJMethodPsiUtils.getSelectorStringFromSelectorStrings(selectorStrings)

    override val returnType: ObjJClassTypeName
        get() = ObjJClassType.getClassType(returnTypeAsString)

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }
}
