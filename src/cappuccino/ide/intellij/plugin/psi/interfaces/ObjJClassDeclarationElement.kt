package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.psi.ObjJAccessorProperty
import cappuccino.ide.intellij.plugin.psi.ObjJClassName
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub

interface ObjJClassDeclarationElement<StubT : ObjJClassDeclarationStub<*>> : ObjJStubBasedElement<StubT>, ObjJIsOfClassType, ObjJHasProtocolList, ObjJCompositeElement, ObjJResolveableElement<StubT> {

    fun getClassNameString() : String

    fun getMethodHeaders() : List<ObjJMethodHeader>

    fun getMethodsHeader(internalOnly:Boolean) : List<ObjJMethodHeader>

    fun getAccessors(internalOnly: Boolean) : List<ObjJAccessorProperty>

    fun getAllSelectors(internalOnly: Boolean) : Set<String>

    fun getClassName() : ObjJClassName?

    fun hasMethod(selector: String): Boolean
}
