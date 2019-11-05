package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.contributor.objJClassAsJsClass
import cappuccino.ide.intellij.plugin.jstypedef.contributor.*
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefTypeAliasIndex
import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import com.intellij.openapi.project.Project

internal fun getAllPropertiesWithNameInClasses(name:String, parentTypes:InferenceResult, static:Boolean, project:Project) : List<JsNamedProperty> {
    val allPropertiesInClasses = getAllPropertiesFromClassNamesList(parentTypes, static, project);
    return allPropertiesInClasses.filter {
        it.name == name
    }
}

internal fun getAllPropertyTypesWithNameInClasses(name:String, parentTypes:InferenceResult, static:Boolean, project:Project) : Set<JsTypeListType> {
    return getAllPropertiesWithNameInClasses(name, parentTypes, static, project).flatMap {
        if (it is JsTypeDefNamedProperty)
            it.types.types
        else if (it is JsTypeListType.JsTypeListFunctionType)
            it.returnType?.types.orEmpty()
        else
            throw Exception("Unexpected JsNamedProperty type: ${it::class.java.simpleName}")
    }.toSet()
}

internal fun getAllPropertiesFromClassNamesList(parentTypes: InferenceResult, static:Boolean, project: Project) : List<JsNamedProperty> {
    // Create class names list
    val childClassNameStrings = (parentTypes.classes + (if (parentTypes.types.any { it is JsTypeListType.JsTypeListArrayType }) "Array" else null)).filterNotNull()

    val classesWithSuperTypes = childClassNameStrings.mapNotNull {
        getClassDefinition(project, it)
    }.withAllSuperClassNames(project)

    // Get Properties for each class
    return classesWithSuperTypes.flatMap {
        getAllProperties(it, static, parentTypes, project)
    }
}

private fun getAllProperties(className: String, static: Boolean, parentTypes: InferenceResult, project: Project): List<JsNamedProperty> {
    // Class is union type, return properties for each class in union
    if (className.contains("&")) {
        return className.split("\\s*&\\s*".toRegex()).flatMap {
            getAllProperties(it, static, parentTypes, project)
        }
    }
    val objjProperties: Set<JsNamedProperty>? = if (!static) {
        objJClassAsJsClass(project, className)?.let {
            val classType = listOf(it).collapseWithSuperType(project)
            classType.properties
        }
    } else null
    val allClassPropertiesRaw: List<JsNamedProperty> = JsTypeDefClassesByNameIndex.instance[className, project].flatMap { classElement ->
        classElement.propertyList.toNamedPropertiesList() + classElement.functionList.map { it.toJsFunctionType() }
    } + objjProperties.orEmpty()
    val arrayPropertiesIfNeededRaw =
            if (parentTypes.types.any { it is JsTypeListType.JsTypeListArrayType })
                JsTypeDefClassesByNameIndex.instance["Array", project].flatMap { arrayClass ->
                    arrayClass.propertyList.toNamedPropertiesList() + arrayClass.functionList.map { it.toJsFunctionType() }
                }
            else
                emptyList()
    val aliasProperties: List<JsNamedProperty> = getJsTypeDefAliasProperties(className, static, parentTypes, project)
    val allProperties = allClassPropertiesRaw + arrayPropertiesIfNeededRaw + aliasProperties
    return allProperties
}


private fun getJsTypeDefAliasProperties(className: String, static: Boolean, parentTypes: InferenceResult, project: Project): List<JsNamedProperty> {
    return JsTypeDefTypeAliasIndex.instance[className, project].flatMap { typeAlias ->
        typeAlias.typesList.functionTypes +
                typeAlias.typesList.interfaceTypes.flatMap { if (static) it.staticProperties else it.instanceProperties } +
                typeAlias.typesList.interfaceTypes.flatMap { if (static) it.staticFunctions else it.instanceFunctions } +
                typeAlias.typeList.flatMap { typeNameElement ->
                    typeNameElement.typeName?.text
                            ?.let {
                                getAllProperties(it, static, parentTypes, project);
                            }.orEmpty()
                }
    }
}
