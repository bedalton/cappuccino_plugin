package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.caches.ObjJMethodHeaderCache
import cappuccino.ide.intellij.plugin.caches.ObjJMethodHeaderDeclarationCache
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderDeclarationStub

interface ObjJMethodHeaderDeclaration<StubT:ObjJMethodHeaderDeclarationStub<*>>:ObjJStubBasedElement<StubT>, ObjJHasMethodSelector {

    fun getCachedReturnType(tag:Long):InferenceResult?

    fun getReturnTypes(tag:Long): Set<String>

    val explicitReturnType:String

    val methodScope: ObjJMethodPsiUtils.MethodScope

    val isStatic: Boolean
        get() = methodScope == ObjJMethodPsiUtils.MethodScope.STATIC

    val methodHeaderCache:ObjJMethodHeaderDeclarationCache
}