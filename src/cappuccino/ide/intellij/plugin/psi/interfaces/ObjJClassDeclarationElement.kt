package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.psi.ObjJClassName
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub
import cappuccino.ide.intellij.plugin.psi.types.ObjJClassType
import cappuccino.ide.intellij.plugin.psi.utils.isUniversalMethodCaller

interface ObjJClassDeclarationElement<StubT : ObjJClassDeclarationStub<*>> : ObjJStubBasedElement<StubT>, ObjJIsOfClassType, ObjJHasProtocolList, ObjJCompositeElement, ObjJResolveableElement<StubT> {

    override val classType: ObjJClassType
        get() {
            val classNameString = getClassNameString()
            return if (!isUniversalMethodCaller(classNameString)) ObjJClassType.getClassType(classNameString) else ObjJClassType.UNDEF
        }

    fun getClassNameString() : String

    fun getMethodHeaderList() : List<ObjJMethodHeader>

    fun getClassName() : ObjJClassName?

    fun hasMethod(selector: String): Boolean
}
