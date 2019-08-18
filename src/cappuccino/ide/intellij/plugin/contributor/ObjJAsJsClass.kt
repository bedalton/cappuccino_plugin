package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsClassDefinition
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeDefNamedProperty
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.toInferenceResult
import cappuccino.ide.intellij.plugin.jstypedef.psi.utils.CompletionModifier
import cappuccino.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration
import cappuccino.ide.intellij.plugin.utils.substringFromEnd
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project


internal fun objJClassAsJsClass(project: Project, className: String): JsClassDefinition? {
    if (DumbService.isDumb(project))
        return null
    val implementations = ObjJImplementationDeclarationsIndex.instance[className, project]
    val properties: MutableList<JsTypeDefNamedProperty> = mutableListOf()
    val extends = mutableListOf<String>()
    for (objClass in implementations) {
        //ProgressManager.checkCanceled()
        val superClassName = objClass.superClassName
        if (superClassName != null)
            extends.add(superClassName)
        extends.addAll(objClass.getInheritedProtocols())
        for (instanceVar in objClass.instanceVariableList?.instanceVariableDeclarationList ?: emptyList()) {
            val property = instanceVar.toJsProperty() ?: continue
            properties.add(property)
        }
    }
    val protocols = ObjJProtocolDeclarationsIndex.instance[className, project]
    for (objClass in protocols) {
        //ProgressManager.checkCanceled()
        extends.addAll(objClass.getInheritedProtocols())
        for (instanceVar in objClass.instanceVariableDeclarationList) {
            val property = instanceVar.toJsProperty() ?: continue
            properties.add(property)
        }
    }
    if (protocols.isEmpty() && implementations.isEmpty())
        return null
    return JsClassDefinition(
            className = className,
            properties = properties.toSet(),
            extends = (extends.map { JsTypeListType.JsTypeListBasicType(it) } ).toSet(),
            isObjJ = true,
            isStruct = false,
            static = false,
            completionModifier = if (className.startsWith("_")) CompletionModifier.AT_QUIET else CompletionModifier.AT_SUGGEST
    )
}

private fun ObjJInstanceVariableDeclaration.toJsProperty(): JsTypeDefNamedProperty? {
    val propertyName = this.variableName?.text ?: return null
    val formalVariableType = this.formalVariableType
    val type = if (this.formalVariableType.varTypeId?.className != null)
        formalVariableType.varTypeId!!.className!!.text
    else {
        val typeName = formalVariableType.text
        when {
            typeName.endsWith("Ref") -> typeName.substringFromEnd(0, -3)
            typeName.endsWith("Pointer") -> typeName.substringFromEnd(0, "Pointer".length)
            typeName.endsWith("Reference") -> typeName.substringFromEnd(0, "Reference".length)
            else -> typeName
        }
    }
    return JsTypeDefNamedProperty(
            name = propertyName,
            readonly = false,
            static = false,
            comment = null,
            types = listOf(type).toInferenceResult()
    )
}

