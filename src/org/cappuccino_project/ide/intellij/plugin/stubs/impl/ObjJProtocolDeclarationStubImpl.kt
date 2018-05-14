package org.cappuccino_project.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJProtocolDeclarationImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub

open class ObjJProtocolDeclarationStubImpl internal constructor(
        parent: StubElement<*>,
        elementType: IStubElementType<*, *>,
        override val className: String,
        override val inheritedProtocols: List<String>,
        private val shouldResolve: Boolean) : ObjJStubBaseImpl<ObjJProtocolDeclarationImpl>(parent, elementType), ObjJClassDeclarationStub<ObjJProtocolDeclarationImpl> {

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }
}