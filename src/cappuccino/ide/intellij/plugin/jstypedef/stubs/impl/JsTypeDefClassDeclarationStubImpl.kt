package cappuccino.ide.intellij.plugin.jstypedef.stubs.impl

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefClassElementImpl
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefInterfaceElementImpl
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefModuleNameImpl
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefClassDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefClassDeclarationStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefClassStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefInterfaceStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefModuleNameStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefClassDeclarationStubType
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefClassStubType
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefStubTypes
import com.intellij.psi.stubs.StubElement

class JsTypeDefClassDeclarationStubImpl<PsiT:JsTypeDefClassDeclaration<*>, StubT:
    JsTypeDefClassDeclarationStub<PsiT>>(parent:StubElement<*>, stubType:JsTypeDefClassDeclarationStubType<PsiT, StubT>, override val fileName:String, override val enclosingNamespaceComponents: List<String>, override val className: String, override val superTypes: Set<JsTypeListType>) : JsTypeDefStubBaseImpl<PsiT>(parent, stubType), JsTypeDefClassDeclarationStub<PsiT> {
    override val enclosingNamespace: String
        get() = enclosingNamespaceComponents.joinToString(".")
}

class JsTypeDefClassStubImpl(
        parent:StubElement<*>,
        override val fileName:String,
        override val enclosingNamespaceComponents: List<String>,
        override val className: String,
        override val superTypes: Set<JsTypeListType>
) : JsTypeDefStubBaseImpl<JsTypeDefClassElementImpl>(parent, JsTypeDefStubTypes.JS_CLASS), JsTypeDefClassStub {
    override val enclosingNamespace: String
        get() = enclosingNamespaceComponents.joinToString(".")
}


class JsTypeDefInterfaceStubImpl(
        parent:StubElement<*>,
        override val fileName:String,
        override val enclosingNamespaceComponents: List<String>,
        override val className: String,
        override val superTypes: Set<JsTypeListType>
) : JsTypeDefStubBaseImpl<JsTypeDefInterfaceElementImpl>(parent, JsTypeDefStubTypes.JS_CLASS), JsTypeDefInterfaceStub {
    override val enclosingNamespace: String
        get() = enclosingNamespaceComponents.joinToString(".")
}