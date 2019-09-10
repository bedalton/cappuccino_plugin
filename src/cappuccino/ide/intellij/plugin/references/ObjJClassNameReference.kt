package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex
import cappuccino.ide.intellij.plugin.jstypedef.indices.JsTypeDefClassesByNameIndex
import cappuccino.ide.intellij.plugin.jstypedef.psi.JsTypeDefType
import cappuccino.ide.intellij.plugin.psi.ObjJClassName
import cappuccino.ide.intellij.plugin.psi.ObjJInheritedProtocolList
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.utils.getPreviousNonEmptySibling
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import java.util.*

class ObjJClassNameReference(element: ObjJClassName) : PsiPolyVariantReferenceBase<ObjJClassName>(element, TextRange.create(0, element.textLength)) {
    private val className: String? = element.text
    private val inProtocol:Boolean = element.parent is ObjJInheritedProtocolList
    private val isClassDeclarationName:Boolean = myElement.parent as? ObjJClassDeclarationElement<*> != null && myElement.getPreviousNonEmptySibling(true)?.text ?: "" != ":"

    override fun handleElementRename(newElementName: String): PsiElement? {
        return if (myElement is ObjJClassName)  myElement.setName(newElementName) else myElement
    }

    override fun isReferenceTo(element: PsiElement): Boolean {
        if (className == null) {
            return false
        }

        if (element !is ObjJClassName || element.text != className) {
            return false
        }


        if (super.isReferenceTo(element)) {
            return true
        }

        if (element is JsTypeDefType)
            return true;

        if (isClassDeclarationName) {
            //return element.parent !is ObjJClassDeclarationElement<*>
        }
        return element.parent is ObjJClassDeclarationElement<*>
    }

    override fun multiResolve(b: Boolean): Array<ResolveResult> {
        if (className == null) {
            return ResolveResult.EMPTY_ARRAY
        }
        val project = myElement.project
        if (DumbService.isDumb(project)) {
            return ResolveResult.EMPTY_ARRAY
        }
        if (isClassDeclarationName) {
            return ResolveResult.EMPTY_ARRAY
        }
        val classNames = ArrayList<PsiElement>()
        val classDeclarations = if (inProtocol) ObjJProtocolDeclarationsIndex.instance[className, project] else ObjJClassDeclarationsIndex.instance[className, myElement.project]
        if (classDeclarations.isEmpty()) {
            return ResolveResult.EMPTY_ARRAY
        }

        for (classDec in classDeclarations) {
            val classDecName = classDec.getClassName() ?: continue
            if (classDecName.text.isNotEmpty() && !classDecName.isEquivalentTo(myElement) && classDec.shouldResolve()) {
                classNames.add(classDecName)
            }
        }
        classNames.addAll(getJsTypedefClasses())
        return PsiElementResolveResult.createResults(classNames)
    }

    private fun getJsTypedefClasses():List<PsiElement> {
        if (className.isNullOrBlank())
            return emptyList<PsiElement>()
        return JsTypeDefClassesByNameIndex.instance[className, myElement.project].mapNotNull {
            it.typeName
        }
    }

    override fun getVariants(): Array<Any> {
        val keys = ArrayList<Any>(ObjJClassDeclarationsIndex.instance.getAllResolvableKeys(myElement.project))
        return keys.toTypedArray()
    }
}
