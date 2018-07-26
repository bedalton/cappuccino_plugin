package cappuccino.ide.intellij.plugin.references

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex
import cappuccino.ide.intellij.plugin.psi.ObjJClassName
import cappuccino.ide.intellij.plugin.psi.ObjJInheritedProtocolList
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptyNode
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptySibling
import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.*
import javafx.scene.control.ProgressIndicator

import java.util.ArrayList

class ObjJClassNameReference(element: ObjJClassName) : PsiPolyVariantReferenceBase<ObjJClassName>(element, TextRange.create(0, element.textLength)) {
    private val className: String? = element.text
    private val inProtocol:Boolean = element.parent is ObjJInheritedProtocolList
    private val isClassDeclarationName:Boolean = myElement.parent as? ObjJClassDeclarationElement<*> != null && myElement.getPreviousNonEmptySibling(true)?.text ?: "" != ":"

    override fun handleElementRename(newElementName: String?): PsiElement {
        return if (myElement is ObjJClassName && newElementName != null)  myElement.setName(newElementName) else myElement
    }

    override fun isReferenceTo(element: PsiElement?): Boolean {
        if (className == null) {
            return false
        }

        if (element !is ObjJClassName || element.text != className) {
            return false
        }

        if (super.isReferenceTo(element)) {
            return true
        }

        if (isClassDeclarationName) {
            //return element.parent !is ObjJClassDeclarationElement<*>
        }
        return element.parent is ObjJClassDeclarationElement<*>
    }

    override fun multiResolve(b: Boolean): Array<ResolveResult> {
        if (className == null) {
            return arrayOf()
        }
        ProgressIndicatorProvider.checkCanceled()
        if (DumbService.isDumb(myElement.project)) {
            return ResolveResult.EMPTY_ARRAY
        }
        if (isClassDeclarationName) {
            return ResolveResult.EMPTY_ARRAY
        }
        val project = myElement.project;
        val classNames = ArrayList<ObjJClassName>()
        val classDeclarations = if (inProtocol) ObjJProtocolDeclarationsIndex.instance[className, project] else ObjJClassDeclarationsIndex.instance[className, myElement.project]
        if (classDeclarations.isEmpty()) {
            return ResolveResult.EMPTY_ARRAY
        }

        for (classDec in classDeclarations) {
            val classDecName = classDec.getClassName() ?: continue;
            if (!classDecName.text.isEmpty() && !classDecName.isEquivalentTo(myElement) && classDec.shouldResolve()) {
                classNames.add(classDecName)
            }
        }
        return PsiElementResolveResult.createResults(classNames)
    }

    override fun getVariants(): Array<Any> {
        val keys = ArrayList<Any>(ObjJClassDeclarationsIndex.instance.getAllResolveableKeys(myElement.project))
        return keys.toTypedArray()
    }
}
