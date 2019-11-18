package cappuccino.ide.intellij.plugin.references

import cappuccino.ide.intellij.plugin.jstypedef.references.JsTypeDefTypeNameReference
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJQualifiedReferenceComponent
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult

class ObjJDocCommentNameElementReference(val namedElement: ObjJQualifiedReferenceComponent)
    : PsiPolyVariantReferenceBase<ObjJQualifiedReferenceComponent>(namedElement, TextRange.create(0, namedElement.text.length)
) {

    override fun isReferenceTo(element: PsiElement): Boolean {
        return super.isReferenceTo(element)
    }

    override fun multiResolve(partial: Boolean): Array<ResolveResult> {
        val classNames = JsTypeDefTypeNameReference.findClassReferencesByName(myElement.text, myElement.project)
        val variables = ObjJVariableReference(namedElement).multiResolve(partial)
        return classNames + variables
    }
}