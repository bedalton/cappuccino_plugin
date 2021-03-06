package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.Tag
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJImplementationStub
import cappuccino.ide.intellij.plugin.stubs.stucts.ObjJMethodStruct

interface ObjJClassDeclarationElement<StubT : ObjJClassDeclarationStub<*>> : ObjJStubBasedElement<StubT>, ObjJIsOfClassType, ObjJHasProtocolList, ObjJCompositeElement, ObjJResolveableElement<StubT> {

    val className: ObjJClassName?

    val classNameString : String

    val internalMethodHeaders : List<ObjJMethodHeader>

    fun getMethodHeaders(internalOnly:Boolean) : List<ObjJMethodHeader>

    fun getMethodStructs(internalOnly:Boolean, tag: Tag) : List<ObjJMethodStruct>

    fun getAccessors(internalOnly: Boolean) : List<ObjJAccessorProperty>

    fun getAllSelectors(internalOnly: Boolean) : Set<String>


    fun hasMethod(selector: String): Boolean

    fun getMethodReturnType(selector:String, tag: Tag) : InferenceResult?

    fun isInstance(className:String) : Boolean

    val inheritedProtocolDeclarations: List<ObjJProtocolDeclaration>

}

interface ObjJImplementationDeclarationElement : ObjJClassDeclarationElement<ObjJImplementationStub> {
    val superClassDeclarations:List<ObjJImplementationDeclaration>
}