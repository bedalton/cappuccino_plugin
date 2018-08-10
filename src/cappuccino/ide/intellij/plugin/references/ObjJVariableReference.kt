package cappuccino.ide.intellij.plugin.references

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.util.IncorrectOperationException
import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import sun.tools.tree.IfStatement
import java.util.logging.Logger

import com.intellij.psi.util.PsiTreeUtil.findCommonContext
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameUtil.getQualifiedNameAsString
import cappuccino.ide.intellij.plugin.psi.utils.ReferencedInScope.UNDETERMINED
import com.intellij.psi.util.PsiTreeUtil
import java.util.logging.Level

class ObjJVariableReference(
        element: ObjJVariableName) : PsiReferenceBase<ObjJVariableName>(element, TextRange.create(0, element.textLength)) {
    private val fqName: String = getQualifiedNameAsString(element)
    private var allInheritedClasses: List<String>? = null
    private var referencedInScope: ReferencedInScope? = null

    private val globalVariableNameElement: PsiElement?
        get() {
            if (DumbService.isDumb(myElement.project)) {
                return null
            }
            val file = myElement.containingObjJFile
            val imports = file?.importStrings
            val globalVariableDeclarations = ObjJGlobalVariableNamesIndex.instance[myElement.text, myElement.project]
            var namedElement:PsiElement? = null
            if (!globalVariableDeclarations.isEmpty()) {
                if (imports == null) {
                    namedElement = globalVariableDeclarations[0].variableName
                } else {
                    for (declaration in globalVariableDeclarations) {
                        if (imports.contains(ObjJFileUtil.getContainingFileName(declaration.containingFile))) {
                            namedElement = declaration.variableName
                        }
                    }
                }
            }
            val functionDeclarationElements = ObjJFunctionsIndex.instance[myElement.text, myElement.project]
            if (namedElement == null && !functionDeclarationElements.isEmpty()) {
                namedElement = functionDeclarationElements.get(0).functionNameNode
                if (namedElement == null) {
                    for (declarationElement in functionDeclarationElements) {
                        namedElement = declarationElement.functionNameNode
                        if (namedElement != null) {
                            break
                        }
                    }
                }
            }
            return if (namedElement != null && !namedElement.isEquivalentTo(myElement)) namedElement else null
        }

    init {
        //LOGGER.log(Level.INFO, "Creating reference resolver for var <"+element.getName()+"> in file: <"+ObjJFileUtil.getContainingFileName(element.getContainingFile())+">");
    }

    private fun getAllInheritedClasses(): List<String> {
        var classes = allInheritedClasses
        if (classes != null) {
            return classes
        }
        classes = ObjJInheritanceUtil.getAllInheritedClasses(myElement.containingClassName, myElement.project)
        allInheritedClasses = classes
        return classes
    }

    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String): PsiElement {
        val parent = element.parent
        val newVariableName = ObjJElementFactory.createVariableName(myElement.project, newElementName)
        parent.node.replaceChild(myElement.node, newVariableName.node)
        return newVariableName
    }

    override fun isReferenceTo(psiElement: PsiElement): Boolean {
        if (element.containingFile.text.startsWith("@STATIC;")) {
            return false
        }
        if (psiElement.text != myElement.text || psiElement.isEquivalentTo(myElement)) {
            return false
        }
        if (psiElement is ObjJClassName) {
            return true
        }

        val referencedElement = resolve(true)
        if (referencedElement?.isEquivalentTo(psiElement) == true) {
            //LOGGER.log(Level.INFO, "Is reference to self in file: ${psiElement.containingFile.name} to item in file ${referencedElement.containingFile.name}")
            return true
        }

        if (psiElement is ObjJVariableName) {
            if (psiElement.indexInQualifiedReference > 0) {
                return false
            }
        }

        //Finds resolved element scope if possible
        if (referencedInScope == null) {
            referencedInScope = referencedElement?.getContainingScope() ?: myElement.getContainingScope()
        }

        //Finds this elements, and the new elements scope
        val sharedContext:PsiElement? = PsiTreeUtil.findCommonContext(myElement, psiElement)
        val sharedScope:ReferencedInScope = sharedContext?.getContainingScope() ?: UNDETERMINED;
        //LOGGER.log(Level.INFO, "Shared context is ${sharedContext.getElementType().toString()}; scope is: ${sharedScope.toString()} for var: ${myElement.text}")
        if (sharedScope == UNDETERMINED && referencedInScope != UNDETERMINED) {
            return false
        }
        if (referencedInScope != UNDETERMINED && referencedInScope == sharedScope) {
            return true
        }
        //If
        if (sharedScope != UNDETERMINED) {
            //LOGGER.log(Level.INFO, "Mismatched Shared scope: SharedIn: " + sharedScope.toString() + "; VariableScope: <" + referencedInScope?.toString() + ">")
        }

        return false//referencedInScope == ReferencedInScope.UNDETERMINED && sharedScope != ReferencedInScope.UNDETERMINED
    }

    override fun resolve(): PsiElement? {
        return resolve(false)
    }

    private fun resolve(nullIfSelfReferencing:Boolean) : PsiElement? {
        try {
            if (myElement.containingFile.text.startsWith("@STATIC;")) {
                return null
            }
        } catch (ignored:Exception) {
            //Exception was thrown on failed attempts at adding code to file pragmatically
            return null
        }
        var variableName = ObjJVariableNameResolveUtil.getVariableDeclarationElement(myElement)
        if (variableName == null) {
            variableName = globalVariableNameElement
        }
        if (nullIfSelfReferencing) {
            return variableName
        }
        return variableName ?: myElement
    }

    override fun getVariants(): Array<Any> {
        return arrayOf()
    }

    companion object {
        private val LOGGER = Logger.getLogger(ObjJVariableReference::class.java.name)
    }

}
