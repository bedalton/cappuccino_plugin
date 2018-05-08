package org.cappuccino_project.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJAccessorPropertyImpl
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJAccessorPropertyStub
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils

import java.util.Arrays
import java.util.Collections

class ObjJAccessorPropertyStubImpl(parent: StubElement<*>, override val containingClass: String,
                                   varType: String?, variableName: String?,
                                   getter: String?, setter: String?,
                                   private val shouldResolve: Boolean
) : ObjJStubBaseImpl<ObjJAccessorPropertyImpl>(parent, ObjJStubTypes.ACCESSOR_PROPERTY), ObjJAccessorPropertyStub {
    override val variableName: String?
    override val getter: String?
    override val setter: String?
    override val varType: String?

    override val paramTypes: List<String>
        get() = if (getter != null) emptyList() else listOf<String>(varType)

    override val selectorStrings: List<String>
        get() = Arrays.asList(*selectorString.split(ObjJMethodPsiUtils.SELECTOR_SYMBOL.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())

    override val selectorString: String
        get() = getter ?: (setter ?: ObjJMethodPsiUtils.EMPTY_SELECTOR)

    override val isRequired: Boolean
        get() = false

    override val returnType: ObjJClassType
        get() = if (getter != null) ObjJClassType.getClassType(varType) else ObjJClassType.VOID

    override val returnTypeAsString: String
        get() = if (getter != null) varType else ObjJClassType.VOID_CLASS_NAME

    override val isStatic: Boolean
        get() = false

    init {
        this.varType = if (varType != null && !varType.isEmpty()) varType else null
        this.variableName = if (variableName != null && !variableName.isEmpty()) variableName else null
        this.getter = if (getter != null && !getter.isEmpty()) getter else null
        this.setter = if (setter != null && !setter.isEmpty()) setter else null
    }

    override fun getContainingClassName(): String {
        return containingClass
    }

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }
}
