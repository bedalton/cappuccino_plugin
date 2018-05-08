package org.cappuccino_project.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJSelectorLiteralImpl
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJSelectorLiteralStub
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import java.util.Collections

class ObjJSelectorLiteralStubImpl(parent: StubElement<*>, override val containingClassName: String, override val selectorStrings: List<String>, private val shouldResolve: Boolean) : ObjJStubBaseImpl<ObjJSelectorLiteralImpl>(parent, ObjJStubTypes.SELECTOR_LITERAL), ObjJSelectorLiteralStub {
    override val selectorString: String

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

    init {
        this.selectorString = ArrayUtils.join(selectorStrings, ObjJMethodPsiUtils.SELECTOR_SYMBOL, true)
    }

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }

}
