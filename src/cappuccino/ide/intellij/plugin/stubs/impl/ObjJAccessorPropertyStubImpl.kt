package cappuccino.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.StubElement
import cappuccino.ide.intellij.plugin.psi.impl.ObjJAccessorPropertyImpl
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassTypeName
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJAccessorPropertyStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils

import java.util.Arrays

class ObjJAccessorPropertyStubImpl(parent: StubElement<*>, override val containingClass: String,
                                   varType: String?, variableName: String?,
                                   getter: String?, setter: String?,
                                   private val shouldResolve: Boolean
) : ObjJStubBaseImpl<ObjJAccessorPropertyImpl>(parent, ObjJStubTypes.ACCESSOR_PROPERTY), ObjJAccessorPropertyStub {
    override val variableName: String? = if (variableName != null && variableName.isEmpty()) variableName else null
    override val getter: String? = if (getter != null && !getter.isEmpty()) getter else null
    override val setter: String? = if (setter != null && !setter.isEmpty()) setter else null
    override val varType: String? = if (varType != null && !varType.isEmpty()) varType else null
    override val containingClassName: String = containingClass

    override val ignored:Boolean = false

    override val paramTypes: List<String>
        get() = if (getter != null || varType == null) emptyList() else listOf(varType)

    override val selectorStrings: List<String>
        get() = Arrays.asList(*selectorString.split(ObjJMethodPsiUtils.SELECTOR_SYMBOL.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())

    override val selectorString: String
        get() = getter ?: (setter ?: ObjJMethodPsiUtils.EMPTY_SELECTOR)

    override val isRequired: Boolean
        get() = false

    override val explicitReturnType: String
        get() = if (getter != null && varType != null) varType else ObjJClassType.VOID_CLASS_NAME

    override val isStatic: Boolean
        get() = false
    override fun shouldResolve(): Boolean {
        return shouldResolve
    }
}
