package org.cappuccino_project.ide.intellij.plugin.psi.interfaces

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJClassName
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodHeader
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType
import org.cappuccino_project.ide.intellij.plugin.psi.utils.isUniversalMethodCaller

interface ObjJClassDeclarationElement<StubT : ObjJClassDeclarationStub<*>> : ObjJStubBasedElement<StubT>, ObjJIsOfClassType, ObjJHasProtocolList, ObjJCompositeElement, ObjJResolveableElement<StubT> {

    override val classType: ObjJClassType
        get() {
            val classNameString = classNameString
            return if (!isUniversalMethodCaller(classNameString)) ObjJClassType.getClassType(classNameString) else ObjJClassType.UNDEF
        }

    val classNameString: String

   fun getMethodHeaderList() : List<ObjJMethodHeader>

    val className: ObjJClassName?

    fun hasMethod(selector: String): Boolean
}
