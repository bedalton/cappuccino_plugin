package cappuccino.ide.intellij.plugin.psi.interfaces

import cappuccino.ide.intellij.plugin.hints.ObjJFunctionDescription
import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.inference.INFERRED_EMPTY_TYPE
import cappuccino.ide.intellij.plugin.inference.anyTypes
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefFunctionArgument
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.psi.ObjJFormalParameterArg
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJFunctionLiteral
import cappuccino.ide.intellij.plugin.psi.ObjJPreprocessorDefineFunction
import cappuccino.ide.intellij.plugin.psi.utils.docComment
import cappuccino.ide.intellij.plugin.universal.psi.ObjJUniversalPsiElement
import cappuccino.ide.intellij.plugin.utils.isNotNullOrEmpty
import cappuccino.ide.intellij.plugin.utils.or

interface ObjJUniversalFunctionElement : ObjJUniversalPsiElement {
    val functionNameString: String?
    val description: ObjJFunctionDescription
}


fun ObjJFunctionDeclarationElement<*>.toJsTypeListType(): JsTypeListType.JsTypeListFunctionType {
    val comment = docComment
    val params = when (this) {
        is ObjJFunctionDeclaration -> this.formalParameterList?.formalParameterArgList
        is ObjJFunctionLiteral -> this.formalParameterList?.formalParameterArgList
        is ObjJPreprocessorDefineFunction -> this.formalParameterList?.formalParameterArgList
        else -> emptyList()
    } ?: emptyList()

    val returnTypeString = stub?.returnType
    val returnTypesList = comment?.getReturnTypes(project)?.toJsTypeList() ?: if (returnTypeString != null && returnTypeString !in anyTypes)
        setOf(returnTypeString).toJsTypeList()
    else
        comment?.getReturnTypes(project)?.toJsTypeList()
    val returnType = if (returnTypesList.isNotNullOrEmpty())
        InferenceResult(types = returnTypesList!!)
    else INFERRED_EMPTY_TYPE

    return JsTypeListType.JsTypeListFunctionType(
            name = stub?.functionName ?: functionNameString,
            comment = null, // @todo implement comment parsing
            parameters = params.mapIndexed { i, it -> it.toJsNamedProperty(comment?.getParameterComment(i)?.possibleClassStrings) },
            returnType = returnType
    )
}


private fun ObjJFormalParameterArg.toJsNamedProperty(typeList: Set<String>?): JsTypeDefFunctionArgument {
    @Suppress("NAME_SHADOWING") val typeList = typeList?.toJsTypeList() ?: emptySet()
    return JsTypeDefFunctionArgument(
            name = this.variableName?.text.or("_"),
            comment = docComment?.commentText,
            types = InferenceResult(types = typeList, nullable = true),
            default = null
    )
}