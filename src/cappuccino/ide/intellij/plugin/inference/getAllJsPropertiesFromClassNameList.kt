package cappuccino.ide.intellij.plugin.inference

import cappuccino.ide.intellij.plugin.contributor.objJClassAsJsClass
import cappuccino.ide.intellij.plugin.jstypedef.contributor.*
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefTypeAliasIndex
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.openapi.project.Project


internal fun getAllPropertyTypesWithNameInParentTypes(name:String, parentTypes:InferenceResult, static:Boolean, project:Project) : Set<JsTypeListType> {
    val properties = getAllPropertiesWithNameInParentTypes(name, parentTypes, static, project)
    return properties.flatMap {
        when (it) {
            is JsTypeDefNamedProperty -> it.types.types
            is JsTypeListType.JsTypeListFunctionType -> it.returnType?.types.orEmpty()
            else -> throw Exception("Unexpected JsNamedProperty type: ${it::class.java.simpleName}")
        }
    }.toSet()
}

internal fun getAllPropertiesWithNameInParentTypes(name:String, parentTypes:InferenceResult, static:Boolean, project:Project) : Set<JsNamedProperty> {
    val allPropertiesInClasses = getAllPropertiesInParentTypes(parentTypes, static, project)
    return allPropertiesInClasses.filter {
        it.name == name
    }.toSet()
}

internal fun getAllPropertiesInParentTypes(parentTypes: InferenceResult, static:Boolean, project: Project) : List<JsNamedProperty> {

    val interfaceProperties = parentTypes.interfaceTypes.flatMap {
        if (static)
            it.staticFunctions + it.staticProperties
        else
            it.instanceFunctions + it.instanceProperties
    }

    val directProperties = parentTypes.properties
            .filter { it.static == static }

    // Create class names list
    val childClassNameStrings = (parentTypes.classes + (if (parentTypes.types.any { it is JsTypeListType.JsTypeListArrayType }) "Array" else null)).filterNotNull()

    val classesWithSuperTypes = childClassNameStrings.mapNotNull {
        //ProgressIndicatorProvider.checkCanceled()
        getClassDefinition(project, it)
    }.withAllSuperClassNames(project)

    // Get Properties for each class
    return interfaceProperties + directProperties + classesWithSuperTypes.flatMap {
        getAllProperties(it, static, parentTypes, project)
    }
}

private fun getAllProperties(className: String, static: Boolean, parentTypes: InferenceResult, project: Project): List<JsNamedProperty> {
    //ProgressIndicatorProvider.checkCanceled()
    // If class is union type, return properties for each class in union
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
    return allClassPropertiesRaw + arrayPropertiesIfNeededRaw + aliasProperties
}


private fun getJsTypeDefAliasProperties(className: String, static: Boolean, parentTypes: InferenceResult, project: Project): List<JsNamedProperty> {
    return JsTypeDefTypeAliasIndex.instance[className, project].flatMap { typeAlias ->
        //ProgressIndicatorProvider.checkCanceled()
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