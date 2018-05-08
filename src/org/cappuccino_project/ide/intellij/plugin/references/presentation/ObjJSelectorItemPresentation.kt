package org.cappuccino_project.ide.intellij.plugin.references.presentation

import com.intellij.navigation.ItemPresentation
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJSelector
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil

import javax.swing.*

class ObjJSelectorItemPresentation(private val selector: ObjJSelector) : ItemPresentation {

    override fun getPresentableText(): String? {
        return ObjJPsiImplUtil.getDescriptiveText(selector)
    }

    override fun getLocationString(): String? {
        val className: String?
        val classDeclarationElement = selector.containingClass
        if (classDeclarationElement is ObjJImplementationDeclaration) {
            val implementationDeclaration = classDeclarationElement as ObjJImplementationDeclaration?
            className = classDeclarationElement.classNameString + if (implementationDeclaration.categoryName != null) " (" + implementationDeclaration.categoryName!!.className.text + ")" else ""
        } else {
            className = classDeclarationElement?.classNameString
        }
        val fileName = ObjJPsiImplUtil.getFileName(selector)
        return (className
                ?: "") + (if (className != null && fileName != null) " " else "") + if (fileName != null) "in $fileName" else ""
    }

    override fun getIcon(b: Boolean): Icon? {
        return null
    }
}
