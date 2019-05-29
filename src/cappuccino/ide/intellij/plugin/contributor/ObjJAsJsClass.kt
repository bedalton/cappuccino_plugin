package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex
import cappuccino.ide.intellij.plugin.psi.ObjJInstanceVariableDeclaration
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettings
import cappuccino.ide.intellij.plugin.utils.substringFromEnd
import com.intellij.openapi.project.Project

fun AllObjJClassesAsJsClasses(project:Project) : List<GlobalJSClass> {
    val classNames = ObjJClassDeclarationsIndex.instance.getAllKeys(project)
    return classNames.mapNotNull {
        objJClassAsJsClass(project, it)
    }
}

fun objJClassAsJsClass(project:Project, className:String) : GlobalJSClass? {
    val implementations = ObjJImplementationDeclarationsIndex.instance[className, project]
    val properties: MutableList<JsNamedProperty> = mutableListOf()
    val extends = mutableListOf<String>()
    for (objClass in implementations) {
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
        extends.addAll(objClass.getInheritedProtocols())
        for (instanceVar in objClass.instanceVariableDeclarationList) {
            val property = instanceVar.toJsProperty() ?: continue
            properties.add(property)
        }
    }
    if (protocols.isEmpty() && implementations.isEmpty())
        return null
    return GlobalJSClass(
            className = className,
            properties = properties.toSet().toList(),
            extends = extends.toSet().toList(),
            isObjJ = true,
            isStruct = false,
            static = false
    )
}

private fun ObjJInstanceVariableDeclaration.toJsProperty() : JsNamedProperty? {
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
    return JsNamedProperty(
            name = propertyName,
            type = type,
            isPublic = !(propertyName.startsWith("_") && ObjJPluginSettings.ignoreUnderscoredClasses),
            nullable = true,
            deprecated = false,
            ignore = false,
            readonly = false
    )
}