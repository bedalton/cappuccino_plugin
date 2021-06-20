package cappuccino.ide.intellij.plugin.psi.impl

import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.inference.Tag
import cappuccino.ide.intellij.plugin.psi.ObjJAccessorProperty
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJImplementationDeclarationElement
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJImplementationStub
import cappuccino.ide.intellij.plugin.stubs.interfaces.ObjJProtocolDeclarationStub
import cappuccino.ide.intellij.plugin.stubs.stucts.ObjJMethodStruct
import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes
import com.intellij.lang.ASTNode


abstract class ObjJProtocolDeclarationMixin : ObjJStubBasedElementImpl<ObjJProtocolDeclarationStub>, ObjJProtocolDeclaration {

    constructor(
            stub: ObjJProtocolDeclarationStub) : super(stub, ObjJStubTypes.PROTOCOL)

    constructor(node: ASTNode) : super(node)

    override fun getAccessors(internalOnly: Boolean): List<ObjJAccessorProperty> {
        return cache.getAccessorProperties(internalOnly)
    }

    override fun getMethodHeaders(internalOnly: Boolean): List<ObjJMethodHeader> {
        return cache.getMethods(internalOnly)
    }

    override fun getMethodStructs(internalOnly:Boolean, tag: Tag) : List<ObjJMethodStruct> {
        return cache.getMethodStructs(internalOnly, tag)
    }

    override fun getAllSelectors(internalOnly: Boolean): Set<String> {
        return cache.getAllSelectors(internalOnly)
    }

    override fun getMethodReturnType(selector:String, tag: Tag) : InferenceResult?
        = cache.getMethodReturnType(selector, tag)

    override val inheritedProtocolDeclarations: List<ObjJProtocolDeclaration>
        get() = cache.inheritedProtocols

    override fun isInstance(className: String): Boolean {
        return inheritedProtocolDeclarations.any { it.classNameString == className}
    }
    override val internalMethodHeaders : List<ObjJMethodHeader>
        get() = getMethodHeaders(true)

}

abstract class ObjJImplementationDeclarationMixin : ObjJStubBasedElementImpl<ObjJImplementationStub>, ObjJImplementationDeclarationElement, ObjJImplementationDeclaration {

    constructor(
            stub: ObjJImplementationStub) : super(stub, ObjJStubTypes.IMPLEMENTATION)

    constructor(node: ASTNode) : super(node)

    override fun getAccessors(internalOnly: Boolean): List<ObjJAccessorProperty> {
        return cache.getAccessorProperties(internalOnly)
    }

    override fun getMethodHeaders(internalOnly: Boolean): List<ObjJMethodHeader> {
        return cache.getMethods(internalOnly)
    }

    override fun getMethodStructs(internalOnly:Boolean, tag: Tag) : List<ObjJMethodStruct> {
        return cache.getMethodStructs(internalOnly, tag)
    }

    override fun getMethodReturnType(selector:String, tag: Tag) : InferenceResult?
            = cache.getMethodReturnType(selector, tag)

    override fun getAllSelectors(internalOnly: Boolean): Set<String> {
        return cache.getAllSelectors(internalOnly)
    }

    override val internalMethodHeaders : List<ObjJMethodHeader>
        get() = getMethodHeaders(true)

    override val superClassDeclarations: List<ObjJImplementationDeclaration>
        get() = cache.superClassDeclarations

    override val inheritedProtocolDeclarations: List<ObjJProtocolDeclaration>
        get() = cache.inheritedProtocols

    override fun isInstance(className: String): Boolean {
        return superClassDeclarations.any { it.classNameString == className || it.superClassName == className } || inheritedProtocolDeclarations.any { it.classNameString == className}
    }
}

val ObjJImplementationDeclaration.isNotCategory:Boolean
    get() = !isCategory