package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsClassDefinition
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.contributor.JsTypeListType.JsTypeListFunctionType
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.JsTypeDefClassDeclaration
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.toJsClassDefinition
import cappuccino.ide.intellij.plugin.jstypedef.stubs.toJsTypeDefTypeListTypes
import com.intellij.openapi.project.Project

interface JsProperty {
    val type: String
    val nullable: Boolean
    val readonly: Boolean
    val comment: String?
    val default: String?
    val callback: JsTypeListFunctionType?
}

private fun Iterable<JsTypeListType>.collapseAllSuperTypeNames(project:Project, captured:MutableSet<String> = mutableSetOf(), out:MutableSet<String> = mutableSetOf()) : Set<String> {
    this.filter { it is JsTypeListType.JsTypeListBasicType }.forEach {basicType ->
        val type = basicType.typeName
        if (type in captured)
            return@forEach
        captured.add(type)
        JsTypeDefClassesByNamespaceIndex.instance[type, project].flatMap { definition:JsTypeDefClassDeclaration<*,*> ->
            definition.extendsStatement?.typeList?.toJsTypeDefTypeListTypes()?.collapseAllSuperTypeNames(project, captured, out).orEmpty()
        }
    }
    return out
}

fun Iterable<JsTypeListType>.collapseToDefinitions(project:Project, captured:MutableSet<String> = mutableSetOf(), out:MutableSet<JsClassDefinition> = mutableSetOf()) : Set<JsClassDefinition> {
    this.filter { it is JsTypeListType.JsTypeListBasicType }.forEach {basicType ->
        val type = basicType.typeName
        if (type in captured)
            return@forEach
        captured.add(type)
        JsTypeDefClassesByNamespaceIndex.instance[type, project].flatMap { definition ->
            out.add(definition.toJsClassDefinition())
            definition.extendsStatement?.typeList?.toJsTypeDefTypeListTypes()?.collapseToDefinitions(project, captured, out).orEmpty()
        }
    }
    return out
}