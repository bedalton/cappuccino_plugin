package cappuccino.ide.intellij.plugin.contributor

import com.intellij.openapi.project.Project


fun getAllObjJAndJsClassObjects(project: Project): List<GlobalJSClass> {
    return globalJSClasses + AllObjJClassesAsJsClasses(project)
}

fun getJsClassObject(project:Project, className: String) : GlobalJSClass? {
    return _getJsClassObject(project, className)?.mergeWithSuperClasses(project)
}

private fun _getJsClassObject(project:Project, className: String) : GlobalJSClass? {
    val out = (globalJSClasses.filter { it.className == className } + AllObjJClassesAsJsClasses(project).filter { it.className == className })
    if (out.isEmpty())
        return null
    return out.flatten(className)
}

fun List<GlobalJSClass>.flatten(className:String) : GlobalJSClass {
    val properties = mutableListOf<JsProperty>()
    val staticProperties= mutableListOf<JsProperty>()
    val functions = mutableListOf<GlobalJSClassFunction>()
    val staticFunctions = mutableListOf<GlobalJSClassFunction>()
    val extendsTemp = mutableListOf<String>()
    for (jsClass in this) {
        properties.addAll(jsClass.properties)
        staticProperties.addAll(jsClass.staticProperties)
        functions.addAll(jsClass.functions)
        staticFunctions.addAll(jsClass.staticFunctions)
        extendsTemp.addAll(jsClass.extends)
    }
    val extends = extendsTemp.flattenNestedSuperClasses()
    return GlobalJSClass(
            className = className,
            properties = properties,
            staticProperties = staticProperties,
            functions = functions,
            extends = extends.toList(),
            staticFunctions = staticFunctions
    )
}

fun GlobalJSClass.mergeWithSuperClasses(project:Project) : GlobalJSClass {
    val superClassNames = extends.flattenNestedSuperClasses()
    val superClasses = superClassNames.mapNotNull { _getJsClassObject(project, it) }
    return (superClasses + this).flatten(className)
}

fun List<String>.flattenNestedSuperClasses() : Set<String> {
    val superClasses = mutableListOf<String>()
    for (jsClass in this) {
        if (superClasses.contains(jsClass))
            continue
        superClasses.add(jsClass)
        addNestedSuperClasses(jsClass, superClasses)
    }
    return superClasses.toSet()
}

private fun addNestedSuperClasses(className:String, superClasses:MutableList<String>) {
    val referenced = globalJSClasses.firstOrNull{
        it.className == className
    } ?: return
    for(superClass in referenced.extends) {
        if (superClasses.contains(superClass))
            continue
        superClasses.add(superClass)
        addNestedSuperClasses(superClass, superClasses)
    }
    return

}