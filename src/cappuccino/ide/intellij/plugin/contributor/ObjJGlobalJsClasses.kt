package cappuccino.ide.intellij.plugin.contributor

import com.intellij.openapi.project.Project
import cappuccino.ide.intellij.plugin.inference.*
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.toJsClassDefinition
import cappuccino.ide.intellij.plugin.jstypedef.stubs.*
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypeListType
import cappuccino.ide.intellij.plugin.jstypedef.stubs.interfaces.JsTypesList

data class JsNamedProperty(
        val name: String,
        override val type: String = "Any",
        val isPublic: Boolean = true,
        override val nullable: Boolean = true,
        override val readonly: Boolean = false,
        override val comment: String? = null,
        override val default: String? = null,
        val ignore: Boolean = false,
        override val callback: JsFunctionType? = null,
        val deprecated: Boolean = false,
        val varArgs: Boolean = type.startsWith("...")
) : JsProperty

val EMPTY_TYPES_LIST = JsTypesList(emptyList(), true)

interface JsProperty {
    val type: String
    val nullable: Boolean
    val readonly: Boolean
    val comment: String?
    val default: String?
    val callback: JsFunctionType?
}

class CollapsedClassType(project:Project, typesList: JsTypesList) {
    private val collapsedClasses:Set<JsClassDefinition> by lazy {
        typesList.types.collapseToDefinitions(project)
    }

    private val collapsedArrayTypes by lazy {
        typesList.arrayTypes.types.collapseToDefinitions(project)
    }


}

private fun List<JsTypeListType>.collapseAllSuperTypeNames(project:Project, captured:MutableSet<String> = mutableSetOf(), out:MutableSet<String> = mutableSetOf()) : Set<String> {
    this.filter { it is JsTypeListType.JsTypeListBasicType }.forEach {basicType ->
        val type = basicType.typeName
        if (type in captured)
            return@forEach
        captured.add(type)
        JsTypeDefClassesByNamespaceIndex.instance[type, project].flatMap { definition ->
            definition.extendsStatement.typeList.toJsTypeDefTypeListTypes().collapseAllSuperTypeNames(project, captured, out)
        }
    }
    return out
}

fun List<JsTypeListType>.collapseToDefinitions(project:Project, captured:MutableSet<String> = mutableSetOf(), out:MutableSet<JsClassDefinition> = mutableSetOf()) : Set<JsClassDefinition> {
    this.filter { it is JsTypeListType.JsTypeListBasicType }.forEach {basicType ->
        val type = basicType.typeName
        if (type in captured)
            return@forEach
        captured.add(type)
        JsTypeDefClassesByNamespaceIndex.instance[type, project].flatMap { definition ->
            out.add(definition.toJsClassDefinition())
            definition.extendsStatement.typeList.toJsTypeDefTypeListTypes().collapseToDefinitions(project, captured, out)
        }
    }
    return out
}