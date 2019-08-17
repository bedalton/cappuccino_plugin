package cappuccino.ide.intellij.plugin.references.presentation

import cappuccino.ide.intellij.plugin.hints.description
import cappuccino.ide.intellij.plugin.psi.ObjJSelector
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil
import cappuccino.ide.intellij.plugin.psi.utils.containingFileName
import cappuccino.ide.intellij.plugin.psi.utils.lineNumber
import com.intellij.navigation.ItemPresentation
import javax.swing.Icon

class ObjJSelectorItemPresentation(private val selector: ObjJSelector) : ItemPresentation {

    override fun getPresentableText(): String? {
        return selector.description?.presentableText ?: return ObjJPsiImplUtil.getDescriptiveText(selector)
    }

    override fun getLocationString(): String? {
        /*val className: String?
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
                ?: "") + (if (className != null && fileName != null) " " else "") + if (fileName != null) "in $fileName" else ""*/
        val lineNumber = selector.lineNumber ?: return selector.containingFileName
        return selector.containingFileName + " #$lineNumber"
    }

    override fun getIcon(b: Boolean): Icon? {
        return null
    }
}
