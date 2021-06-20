package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.caches.ObjJMethodHeaderDeclarationCache
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.Tag
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils
import cappuccino.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils.MethodScope.STATIC
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJMethodHeaderDeclarationStub
import cappuccino.ide.intellij.plugin.stubs.stucts.ObjJSelectorStruct

interface ObjJMethodHeaderDeclaration<StubT:ObjJMethodHeaderDeclarationStub<*>>:ObjJStubBasedElement<StubT>, ObjJHasMethodSelector {

    val selectorStructs:List<ObjJSelectorStruct>

    fun getCachedReturnType(tag: Tag):InferenceResult?

    fun getReturnTypes(tag: Tag): Set<String>

    val explicitReturnType:String

    val methodScope: ObjJMethodPsiUtils.MethodScope

    val isStatic: Boolean
        get() = methodScope == STATIC

    val methodHeaderCache:ObjJMethodHeaderDeclarationCache

    val isPrivate:Boolean
}