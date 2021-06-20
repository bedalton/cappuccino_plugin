package cappuccino.ide.intellij.plugin.utils

import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil.isIn
import com.intellij.psi.PsiElement
import icons.ObjJIcons
import javax.swing.Icon


fun getIcon(element: PsiElement): Icon? {
    if (element is ObjJClassName) {
        val classDeclarationElement = element.getParentOfType(ObjJClassDeclarationElement::class.java)

        val className = element.getText()
        if (classDeclarationElement == null || classDeclarationElement.classNameString != className) {
            return null
        }
        if (classDeclarationElement is ObjJImplementationDeclaration) {
            return if (classDeclarationElement.isCategory) {
                ObjJIcons.CATEGORY_ICON
            } else {
                ObjJIcons.CLASS_ICON
            }
        } else if (classDeclarationElement is ObjJProtocolDeclaration) {
            return ObjJIcons.PROTOCOL_ICON
        }
        return null
    }

    if (element is ObjJFunctionName) {
        return ObjJIcons.FUNCTION_ICON
    }

    if (element is ObjJVariableName) {
        return ObjJIcons.VARIABLE_ICON
    }

    if (element is ObjJSelector) {
        if (element.isIn(ObjJMethodHeaderDeclaration::class.java)) {
            return ObjJIcons.METHOD_ICON
        }
        if (element.isIn(ObjJInstanceVariableDeclaration::class.java)) {
            return ObjJIcons.ACCESSOR_ICON
        }
        if (element.isIn(ObjJSelectorLiteral::class.java)) {
            return ObjJIcons.SELECTOR_ICON
        }
    }
    return null
}