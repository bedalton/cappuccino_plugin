package org.cappuccino_project.ide.intellij.plugin.psi.interfaces

import org.cappuccino_project.ide.intellij.plugin.psi.ObjJClassName
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJMethodHeader
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodCallPsiUtil
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJClassDeclarationStub
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJClassType

interface ObjJClassDeclarationElement<StubT : ObjJClassDeclarationStub<out ObjJClassDeclarationElement<*>>> : ObjJStubBasedElement<StubT>, ObjJIsOfClassType, ObjJHasProtocolList, ObjJCompositeElement, ObjJResolveableElement<StubT> {

    override val classType: ObjJClassType
        get() {
            val classNameString = classNameString
            return if (!ObjJMethodCallPsiUtil.isUniversalMethodCaller(classNameString)) ObjJClassType.getClassType(classNameString) else ObjJClassType.UNDEF
        }

    val classNameString: String
        get() {
            val stub = stub
            if (stub != null) {
                return stub.className
            }
            return if (className != null) className!!.text else ObjJClassType.UNDEF.className
        }

    val methodHeaders: List<ObjJMethodHeader>

    val className: ObjJClassName?

    fun hasMethod(selector: String): Boolean

    override fun getStub(): StubT?
}
