package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.psi.impl.ObjJObjectLiteralImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJObjectLiteralStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.psi.stubs.StubElement

class ObjJObjectLiteralStubImpl(parent:StubElement<*>, override val objectWithoutInference:JsTypeListType.JsTypeListClass?) : ObjJStubBaseImpl<ObjJObjectLiteralImpl>(parent, ObjJStubTypes.OBJECT_LITERAL), ObjJObjectLiteralStub