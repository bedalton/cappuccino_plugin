package cappuccino.ide.intellij.plugin.jstypedef.stubs.impl

import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefVariableDeclarationImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefVariableDeclarationStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefStubTypes
import com.intellij.psi.stubs.StubElement

class JsTypeDefVariableDeclarationStubImpl (
        parent: StubElement<*>,
        override val fileName: String,
        override val variableName:String,
        override val enclosingNamespace: String,
        override val enclosingNamespaceComponents: List<String>,
        override val types: InferenceResult,
        override val readonly: Boolean,
        override val comment: String? = null,
        override val default: String? = null
) : JsTypeDefStubBaseImpl<JsTypeDefVariableDeclarationImpl>(parent, JsTypeDefStubTypes.JS_VARIABLE_DECLARATION), JsTypeDefVariableDeclarationStub  {
    override val type: String
        get() = types.types.joinToString("|") { it.toString() }
    override val callback: JsTypeListType.JsTypeListFunctionType?
        get() = types.functionTypes.firstOrNull()
    override val nullable: Boolean get() = types.nullable
    override val static: Boolean
        get() = true
    override val namespaceComponents:List<String> by lazy {
        enclosingNamespaceComponents + variableName
    }
}