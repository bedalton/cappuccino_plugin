package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex
import cappuccino.ide.intellij.plugin.inference.inferQualifiedReferenceType
import cappuccino.ide.intellij.plugin.inference.toClassList
import cappuccino.ide.intellij.plugin.jstypedef.contributor.withAllSuperClassNames
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefFunctionsByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefPropertiesByNamespaceIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefClassElement
import cappuccino.ide.intellij.plugin.jstypedef.psi.interfaces.toJsClassDefinition
import cappuccino.ide.intellij.plugin.psi.ObjJGlobalVariableDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJQualifiedReferenceComponent
import cappuccino.ide.intellij.plugin.psi.interfaces.previousSiblings
import cappuccino.ide.intellij.plugin.universal.psi.ObjJUniversalPsiElement

internal fun getJsNamedElementsForReferencedElement(psiElement:ObjJQualifiedReferenceComponent, propertyName:String, tag:Long) : List<ObjJUniversalPsiElement> {

    val project = psiElement.project

    // Get simple if no previous siblings
    val prevSiblings = psiElement.previousSiblings
    if (prevSiblings.isEmpty()) {

        val outPropertiesSimple: List<ObjJUniversalPsiElement> = JsTypeDefPropertiesByNamespaceIndex.instance[propertyName, project].filter {
            it.enclosingNamespace.isEmpty()
        }.mapNotNull {
            it.propertyName ?: it.propertyAccess?.propertyName ?: it.stringLiteral
        }
        var outFunctionsSimple: List<ObjJUniversalPsiElement> = JsTypeDefFunctionsByNamespaceIndex.instance[propertyName, project].filter {
            it.enclosingNamespace.isEmpty()
        }.map {
            it.functionName
        }

        if (outFunctionsSimple.isEmpty()) {
            outFunctionsSimple = JsTypeDefClassesByNameIndex.instance[propertyName, project].mapNotNull {
                (it as? JsTypeDefClassElement)?.typeName
            }
        }
        return outFunctionsSimple + outPropertiesSimple
    }

    // Get properties if previous siblings are actually a namespace
    val otherResults:List<ObjJUniversalPsiElement> = if (prevSiblings.isNotEmpty()) {
        val searchString = prevSiblings.joinToString(".") { it.text } + "." + propertyName
        val functions = JsTypeDefFunctionsByNamespaceIndex.instance[searchString, project].map {
            it.functionName
        }
        val properties = JsTypeDefPropertiesByNamespaceIndex.instance[searchString, project].mapNotNull {
            it.propertyName ?: it.propertyAccess?.propertyName ?: it.stringLiteral
        }
        functions + properties
    } else {
        emptyList()
    }

    // Get properties from inferred previous sibling type
    val className = prevSiblings.joinToString("\\.") { Regex.escapeReplacement(it.text) }
    val isStatic = JsTypeDefClassesByNamespaceIndex.instance[className, project].isNotEmpty()
    // Get types if qualified
    val classTypes = prevSiblings.types(tag).ifEmpty { null }
            ?: return emptyList()
    val allClassNames = classTypes.flatMap { className ->
        JsTypeDefClassesByNameIndex.instance[className, project].map { classDeclaration ->
            classDeclaration.toJsClassDefinition()
        }
    }.withAllSuperClassNames(project)
    // Create regex search string with all properties
    val searchString = "(" + allClassNames.joinToString("|") { Regex.escapeReplacement(it) } + ")\\." + propertyName
    val foundFunctions:List<ObjJUniversalPsiElement> = JsTypeDefFunctionsByNamespaceIndex.instance.getByPatternFlat(searchString, project).filter {
        it.isStatic == isStatic
    }.map {
        it.functionName
    }
    val foundProperties:List<ObjJUniversalPsiElement> = JsTypeDefPropertiesByNamespaceIndex.instance.getByPatternFlat(searchString, project).filter {
        it.isStatic == isStatic
    }.mapNotNull {
        it.propertyName ?: it.propertyAccess?.propertyName ?: it.stringLiteral
    }
    return foundFunctions + foundProperties + otherResults
}

private fun List<ObjJQualifiedReferenceComponent>.types(tag:Long) : Set<String> {
    return inferQualifiedReferenceType(this, tag)?.toClassList(null)?.toSet().orEmpty()
}
