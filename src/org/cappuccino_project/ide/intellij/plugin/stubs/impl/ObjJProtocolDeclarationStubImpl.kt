package org.cappuccino_project.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJProtocolDeclarationImpl
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJProtocolDeclarationStub
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes

class ObjJProtocolDeclarationStubImpl(
        parent: StubElement<*>, className: String, protocols: List<String>, shouldResolve: Boolean) : ObjJClassDeclarationStubImpl<ObjJProtocolDeclarationImpl>(parent, ObjJStubTypes.PROTOCOL, className, protocols, shouldResolve), ObjJProtocolDeclarationStub
