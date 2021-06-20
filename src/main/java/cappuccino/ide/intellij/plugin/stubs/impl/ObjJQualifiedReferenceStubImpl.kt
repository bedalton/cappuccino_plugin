package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.psi.impl.ObjJQualifiedReferenceImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJQualifiedReferenceComponentPart
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJQualifiedReferenceStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.psi.stubs.StubElement

class ObjJQualifiedReferenceStubImpl(parent:StubElement<*>, override val components: List<ObjJQualifiedReferenceComponentPart>) : ObjJStubBaseImpl<ObjJQualifiedReferenceImpl>(parent, ObjJStubTypes.QUALIFIED_REFERENCE), ObjJQualifiedReferenceStub