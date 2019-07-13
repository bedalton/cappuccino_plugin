package cappuccino.ide.intellij.plugin.references.presentation

import cappuccino.ide.intellij.plugin.hints.description
import com.intellij.navigation.ItemPresentation
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJSelector
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import com.intellij.openapi.progress.ProgressIndicatorProvider

import javax.swing.*

class ObjJSelectorItemPresentation(private val selector: ObjJSelector) : ItemPresentation {

    override fun getPresentableText(): String? {
        val presentableText = selector.description?.presentableText ?: return ObjJPsiImplUtil.getDescriptiveText(selector)
        //LOGGER.info("Getting PresentableText: $presentableText")
        return presentableText
    }

    override fun getLocationString(): String? {
        val className: String?
        val classDeclarationElement = selector.containingClass
        className = if (classDeclarationElement is ObjJImplementationDeclaration) {
                        val categoryName = classDeclarationElement.categoryNameString
                        classDeclarationElement.classNameString +
                                if (categoryName != null && !categoryName.isEmpty()) {
                                    " ($categoryName)"
                                } else ""
                    } else {
                        classDeclarationElement?.classNameString
                    }
        val fileName = ObjJPsiImplUtil.getFileName(selector)
        return (className
                ?: "") + (if (className != null && fileName != null) " " else "") + if (fileName != null) "in $fileName" else ""
    }

    override fun getIcon(b: Boolean): Icon? {
        return null
    }
}
