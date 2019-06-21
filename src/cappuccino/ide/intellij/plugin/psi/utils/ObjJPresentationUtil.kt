package cappuccino.ide.intellij.plugin.psi.utils

import cappuccino.ide.intellij.plugin.psi.ObjJClassName
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJProtocolDeclaration
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil
import com.intellij.navigation.ItemPresentation
import icons.ObjJIcons
import javax.swing.Icon

/**
 * Gets the presentation object for an @implementation declaration
 */
fun getPresentation(declaration: ObjJImplementationDeclaration): ItemPresentation {
    val text = declaration.classNameString + if (declaration.isCategory) " (${declaration.categoryName?.className?.text})" else ""
    val icon = if (declaration.isCategory) ObjJIcons.CATEGORY_ICON else ObjJIcons.CLASS_ICON
    val fileName = ObjJFileUtil.getContainingFileName(declaration)
    return object : ItemPresentation {
        override fun getPresentableText(): String {
            return text
        }

        override fun getLocationString(): String {
            return fileName ?: ""
        }

        override fun getIcon(b: Boolean): Icon {
            return icon
        }
    }
}

/**
 * Gets presentation for a protocol declaration
 */
fun getPresentation(declaration: ObjJProtocolDeclaration): ItemPresentation {
    val fileName = ObjJFileUtil.getContainingFileName(declaration)
    return object : ItemPresentation {
        override fun getPresentableText(): String {
            return declaration.classNameString
        }

        override fun getLocationString(): String {
            return fileName ?: ""
        }

        override fun getIcon(b: Boolean): Icon {
            return ObjJIcons.PROTOCOL_ICON
        }
    }
}


/**
 * Gets the presentation for a class name element
 */
fun getPresentation(className: ObjJClassName) : ItemPresentation {
    val parent = className.parent as? ObjJClassDeclarationElement<*> ?: return getDummyPresenter(className)
    if (parent is ObjJImplementationDeclaration) {
        return getPresentation(parent)
    } else if (parent is ObjJProtocolDeclaration) {
        return getPresentation(parent)
    }
    return getDummyPresenter(className)
}

/**
 * Gets a default presentation for a given element
 */
private fun getDummyPresenter(psiElement: ObjJCompositeElement) : ItemPresentation {
    val fileName = ObjJFileUtil.getContainingFileName(psiElement)
    return object : ItemPresentation {
        override fun getIcon(p0: Boolean): Icon? {
            return null
        }

        override fun getLocationString(): String? {
            return fileName ?: ""
        }

        override fun getPresentableText(): String? {
            return psiElement.text
        }
    }
}