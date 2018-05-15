package cappuccino.ide.intellij.plugin.references.presentation

import com.intellij.navigation.ItemPresentation
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJSelector
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil

import javax.swing.*

class ObjJSelectorItemPresentation(private val selector: ObjJSelector) : ItemPresentation {

    override fun getPresentableText(): String? {
        return ObjJPsiImplUtil.getDescriptiveText(selector)
    }

    override fun getLocationString(): String? {
        val className: String?
        val classDeclarationElement = selector.containingClass
        className = if (classDeclarationElement is ObjJImplementationDeclaration) {
                        val categoryName = classDeclarationElement.categoryName?.className?.text
                        classDeclarationElement.getClassNameString() +
                                if (categoryName != null && !categoryName.isEmpty()) {
                                    " ($categoryName)"
                                } else ""
                    } else {
                        classDeclarationElement?.getClassNameString()
                    }
        val fileName = ObjJPsiImplUtil.getFileName(selector)
        return (className
                ?: "") + (if (className != null && fileName != null) " " else "") + if (fileName != null) "in $fileName" else ""
    }

    override fun getIcon(b: Boolean): Icon? {
        return null
    }
}
