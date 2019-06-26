package cappuccino.ide.intellij.plugin.actions

import cappuccino.ide.intellij.plugin.indices.ObjJProtocolDeclarationsIndex
import cappuccino.ide.intellij.plugin.psi.ObjJClassName
import cappuccino.ide.intellij.plugin.psi.ObjJImplementationDeclaration
import cappuccino.ide.intellij.plugin.psi.ObjJMethodHeader
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.utils.getSelfOrParentOfType
import com.intellij.ide.util.MemberChooser
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataKeys
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement

class AddProtocolMethodsAction : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val classNameElement = event.elementAs(ObjJClassName::class.java) ?: return
        val project = classNameElement.project
        val enclosingClass = event.enclosingClass as? ObjJImplementationDeclaration ?: return
        val possibleMethods = getPossibleMethodHeader(classNameElement.text, project, enclosingClass)
        val options = possibleMethods.map { it.text }


    }

    private fun getPossibleMethodHeader(className:String, project:Project, enclosingClass:ObjJImplementationDeclaration) : List<ObjJMethodHeader> {
        val allMethods = ObjJProtocolDeclarationsIndex.instance[className, project].flatMap {
            it.methodHeaderList
        }
        val currentClassMethods = enclosingClass.getMethodHeaders(true).map {
            it.toSimpleString()
        }
        val out = mutableListOf<ObjJMethodHeader>()
        val outStrings = mutableListOf<String>()
        allMethods.forEach {
            if (it.toSimpleString() in currentClassMethods)
                return@forEach
            val simpleString = it.toSimpleString()
            if (simpleString in outStrings)
                return@forEach
            outStrings.add(simpleString)
            out.add(it)
        }
        return out
    }

    override fun update(event: AnActionEvent) {
        val shouldShow = event.project != null && event.elementAs(ObjJClassName::class.java) != null && event.enclosingClass as? ObjJImplementationDeclaration != null
        event.presentation.isEnabledAndVisible = shouldShow;
    }
}

private val AnActionEvent.element:PsiElement?
    get() = dataContext.getData(DataKeys.PSI_ELEMENT)

private fun <PsiT:PsiElement> AnActionEvent.elementAs(psiClass:Class<PsiT>) : PsiT?
    = element.getSelfOrParentOfType(psiClass)


private val AnActionEvent.enclosingClass:ObjJClassDeclarationElement<*>?
    get() = (element as? ObjJCompositeElement).getSelfOrParentOfType(ObjJClassDeclarationElement::class.java)

private fun ObjJMethodHeaderDeclaration<*>.toSimpleString() : String {
    return "${methodScope.scopeMarker} ${selectorString}"
}

/*internal class MethodChooser(val methods:List<ObjJMethodHeader>) : MemberChooser<ObjJMethodHeader>(methods.toTypedArray(), true, true, project) {

}*/