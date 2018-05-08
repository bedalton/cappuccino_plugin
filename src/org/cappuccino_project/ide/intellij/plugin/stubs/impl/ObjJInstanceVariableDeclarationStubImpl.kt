package org.cappuccino_project.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJInstanceVariableDeclarationImpl
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJInstanceVariableDeclarationStub
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes

class ObjJInstanceVariableDeclarationStubImpl(parent: StubElement<*>, containingClass: String?, override val varType: String, override val variableName: String, getter: String?, setter: String?, private val shouldResolve: Boolean) : ObjJStubBaseImpl<ObjJInstanceVariableDeclarationImpl>(parent, ObjJStubTypes.INSTANCE_VAR), ObjJInstanceVariableDeclarationStub {

    override val containingClass: String
    override val getter: String?
    override val setter: String?

    init {
        this.containingClass = containingClass ?: ObjJClassType.UNDEF_CLASS_NAME
        this.getter = if (getter != null && getter.length > 0) getter else null
        this.setter = if (setter != null && setter.length > 0) setter else null
    }

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }

}
