package cappuccino.ide.intellij.plugin.jstypedef.stubs.impl

import cappuccino.ide.intellij.plugin.inference.INFERRED_EMPTY_TYPE
import cappuccino.ide.intellij.plugin.inference.InferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefFunctionArgument
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.toFunctionArgumentList
import cappuccino.ide.intellij.plugin.jstypedef.psi.impl.JsTypeDefFunctionImpl
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeDefFunctionStub
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.stubs.types.JsTypeDefStubTypes
import com.intellij.psi.stubs.StubElement

class JsTypeDefFunctionStubImpl(
        parent:StubElement<*>,
        override val fileName:String,
        override val enclosingNamespace:String,
        override val enclosingClass:String?,
        override val functionName: String,
        override val parameters: List<JsTypeDefFunctionArgument>,
        override val returnType: InferenceResult,
        override val global:Boolean,
        override val static: Boolean
) : JsTypeDefStubBaseImpl<JsTypeDefFunctionImpl>(parent, JsTypeDefStubTypes.JS_FUNCTION), JsTypeDefFunctionStub {

    override val asJsFunctionType: JsTypeListType.JsTypeListFunctionType by lazy {
        JsTypeListType.JsTypeListFunctionType(
                name = functionName,
                comment = null, // @todo implement comment parsing
                parameters = parameters,
                returnType = returnType
        )
    }

}