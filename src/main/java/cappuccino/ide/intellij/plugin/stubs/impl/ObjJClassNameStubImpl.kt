package cappuccino.ide.intellij.plugin.stubs.impl

import cappuccino.ide.intellij.plugin.psi.impl.ObjJClassNameImpl
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJClassNameStub
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.psi.stubs.StubElement
import com.intellij.util.io.StringRef

class ObjJClassNameStubImpl(parent:StubElement<*>, private val className:StringRef) : ObjJStubBaseImpl<ObjJClassNameImpl>(parent, ObjJStubTypes.CLASS_NAME), ObjJClassNameStub {

    constructor(parent:StubElement<*>, classNameString:String): this(parent, StringRef.fromString(classNameString))
    override fun getClassName(): String = className.toString()

}