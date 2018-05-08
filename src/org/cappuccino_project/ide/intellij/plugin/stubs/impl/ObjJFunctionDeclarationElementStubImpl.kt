package org.cappuccino_project.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJFunctionDeclarationElementStub
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes

open class ObjJFunctionDeclarationElementStubImpl<PsiT : ObjJFunctionDeclarationElement<out ObjJFunctionDeclarationElementStub<*>>>(parent: StubElement<*>, stubElementType: IStubElementType<*, *>, override val fileName: String, override val fqName: String, override val paramNames: List<String>, override val returnType: String?, private val shouldResolve: Boolean) : ObjJStubBaseImpl<PsiT>(parent, stubElementType), ObjJFunctionDeclarationElementStub<PsiT> {
    override val functionName: String

    override val numParams: Int
        get() = paramNames.size

    init {
        val lastDotIndex = fqName.lastIndexOf(".")
        this.functionName = fqName.substring(if (lastDotIndex >= 0) lastDotIndex else 0)
    }

    override fun shouldResolve(): Boolean {
        return shouldResolve
    }
}
