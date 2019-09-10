package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException
import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJImplementationDeclarationsIndex
import cappuccino.ide.intellij.plugin.psi.ObjJClassName
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import java.util.*


/**
 * Determines whether an @implementation class is a category class
 */
fun isCategory(declaration:ObjJImplementationDeclaration): Boolean =
    declaration.stub?.isCategory ?: declaration.categoryName != null

/**
 * Gets the containing class's super class
 */
fun getContainingSuperClassName(psiElement: ObjJCompositeElement, returnDefault: Boolean) : String? {
    val containingClass = ObjJPsiImplUtil.getContainingClass(psiElement) ?: return null
    val project = psiElement.project
    if (containingClass !is ObjJImplementationDeclaration) {
        return if (returnDefault) containingClass.classNameString else null
    }
    if (!containingClass.isCategory) {
        return containingClass.superClass?.text
    }
    return getCategoryClassBaseDeclaration(containingClass.classNameString, project)?.superClassName ?: if (returnDefault) containingClass.classNameString else null

}

/**
 * Searches through class definitions to find the base class for a category
 */
private fun getCategoryClassBaseDeclaration(classNameString:String, project: Project) : ObjJImplementationDeclaration? {
    val classDeclarations:List<ObjJImplementationDeclaration> = (ObjJImplementationDeclarationsIndex.instance[classNameString, project])
    for (classDeclaration:ObjJImplementationDeclaration in classDeclarations) {
        if (classDeclaration.isCategory) {
            continue
        }
        return classDeclaration
    }
    return null
}

/**
 * Gets the super class of the containing class declaration
 * @param returnDefault **true** if method should return containing class if super class is not found
 * @return containing super class
 */
fun getContainingSuperClass(psiElement:ObjJCompositeElement, returnDefault: Boolean, filter: ((ObjJClassDeclarationElement<*>) -> Boolean)? = null): ObjJClassName? {
    val project = psiElement.project
    if (DumbService.isDumb(project)) {
        return null
    }
    val containingClass = ObjJPsiImplUtil.getContainingClass(psiElement) ?: return null
    val superClassName = getContainingSuperClassName(psiElement, returnDefault) ?: return if (returnDefault) containingClass.getClassName() else null

    val superClassDeclarations = ArrayList<ObjJClassDeclarationElement<*>>(ObjJClassDeclarationsIndex.instance[superClassName, project])
    if (superClassDeclarations.size < 1) {
        return if (returnDefault) containingClass.getClassName() else null
    }
    var className:ObjJClassName? = null
    for (superClassDec in superClassDeclarations) {
        if (superClassDec is ObjJImplementationDeclaration) {
            className = superClassDec.getClassName() ?: continue
            if (filter != null && filter(superClassDec)) {
                return className
            }
            if (superClassDec.isEquivalentTo(containingClass) || superClassDec.isCategory) {
                continue
            }
            if (filter == null) {
                return className
            }
        }
    }
    return className
}

/**
 * Attempts to find all classes which contain a method matching a given selector
 */
fun getContainingClassWithSelector(containingClassName:String, selector:String, defaultName:ObjJClassName) : ObjJClassName {
    val project:Project = defaultName.project
    if (DumbService.isDumb(project)) {
        return defaultName
    }
    val classDeclarations = ArrayList<ObjJClassDeclarationElement<*>>(ObjJClassDeclarationsIndex.instance[containingClassName, project])
    if (classDeclarations.size < 1) {
        return defaultName
    }
    var potential:ObjJClassName? = null
    var className:ObjJClassName? = null
    for (classDeclaration in classDeclarations) {
        if (classDeclaration is ObjJImplementationDeclaration) {
            className = classDeclaration.getClassName() ?: continue
            if (classDeclaration.hasMethod(selector)) {
                if (classDeclaration.isCategory) {
                    potential = className
                } else {
                    return className
                }
            } else if (potential == null) {
                potential = className
            }
        } else if (potential == null) {
            val protocol = classDeclaration as? ObjJProtocolDeclaration ?: continue
            if (protocol.hasMethod(selector)) {
                potential = className
            }
        }
    }
    return potential ?: className ?: defaultName
}



/**
 * Gets the super class name for a class with a given name
 */
fun getSuperClassName(className: String, project: Project): String? {
    if (DumbService.isDumb(project)) {
        throw IndexNotReadyRuntimeException()
    }
    for (declaration in ObjJImplementationDeclarationsIndex.instance[className, project]) {
        if (declaration.superClassName != null) {
            return declaration.superClassName
        }
    }
    return null
}

/**
 * Gets the super class given an implementation class
 */
fun getSuperClassName(declaration: ObjJImplementationDeclaration): String? {
    val stub = declaration.stub
    return if (stub != null) stub.superClassName else declaration.superClass?.text
}

