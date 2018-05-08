package org.cappuccino_project.ide.intellij.plugin.stubs.impl

import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.StubElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJImportStatement
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImportStub

class ObjJImportStubImpl<PsiT : ObjJImportStatement<out ObjJImportStub<PsiT>>>(parent: StubElement<*>, elementType: IStubElementType<*, *>, override val framework: String?, override val fileName: String) : ObjJStubBaseImpl<PsiT>(parent, elementType), ObjJImportStub<PsiT>
