package cappuccino.ide.intellij.plugin.references

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.util.IncorrectOperationException
import cappuccino.ide.intellij.plugin.indices.ObjJFunctionsIndex
import cappuccino.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex
import cappuccino.ide.intellij.plugin.psi.*
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import cappuccino.ide.intellij.plugin.psi.utils.*
import cappuccino.ide.intellij.plugin.utils.ObjJFileUtil
import cappuccino.ide.intellij.plugin.utils.ObjJInheritanceUtil
import sun.tools.tree.IfStatement
import java.util.logging.Logger

import com.intellij.psi.util.PsiTreeUtil.findCommonContext
import cappuccino.ide.intellij.plugin.psi.utils.ObjJVariableNameUtil.getQualifiedNameAsString
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
        if (psiElement.text != myElement.text || psiElement.isEquivalentTo(myElement)) {
            return false
        }
        if (psiElement is ObjJClassName) {
            return true
        }

        //Finds resolved element scope if possible
        if (referencedInScope == null) {
            val referencedElement = resolve(false)
            referencedInScope = referencedElement?.getContainingScope() ?: myElement.getContainingScope()
        }
        //Finds this elements, and the new elements scope
        val sharedScope:ReferencedInScope = myElement.getContainingScope(psiElement)
        if (referencedInScope != ReferencedInScope.UNDETERMINED && referencedInScope == sharedScope) {
            return true
        }
        //If
        if (sharedScope != ReferencedInScope.UNDETERMINED) {
            LOGGER.log(Level.INFO, "Mismatched Shared scope: SharedIn: " + sharedScope.toString() + "; VariableScope: <" + referencedInScope?.toString() + ">")
        }
        return referencedInScope == ReferencedInScope.UNDETERMINED && sharedScope != ReferencedInScope.UNDETERMINED
    }

    /*
    override fun isReferenceTo(psiElement: PsiElement): Boolean {
        var variableNameText = psiElement.text
        if (variableNameText != myElement.text) {
            return false
        }
        if (psiElement is ObjJClassName) {
            return true
        }
        if (referencedInScope == null) {
            var referencedElement = resolve()
            if (referencedElement == null) {
                referencedElement = myElement
            }
            if (referencedElement is ObjJVariableName) {
                referencedInScope = isReferencedInScope(referencedElement)
            } else {
                // Variable does not reference a variable name element
                // most likely null
                // Set scope to this variables surrounding scope
                if (myElement.hasParentOfType(ObjJMethodHeaderDeclaration::class.java)) {
                    referencedInScope = ReferencedInScope.METHOD
                }

                if (myElement.hasParentOfType(ObjJFormalParameterArg::class.java)) {
                    referencedInScope = ReferencedInScope.FUNCTION
                }

                if (myElement.hasParentOfType(ObjJInstanceVariableDeclaration::class.java)) {
                    referencedInScope = ReferencedInScope.CLASS
                }

                if (myElement.hasParentOfType(ObjJBodyVariableAssignment::class.java)) {
                    val bodyVariableAssignment = myElement.getParentOfType(ObjJBodyVariableAssignment::class.java)!!
                    if (bodyVariableAssignment.varModifier == null) {
                        if (bodyVariableAssignment.parent is IfStatement) {
                            referencedInScope = ReferencedInScope.IF
                        }

                        if (bodyVariableAssignment.parent is ObjJIterationStatement) {
                            referencedInScope = ReferencedInScope.ITERATION_HEADER
                        }
                    }
                }
            }
        }
        if (referencedInScope == null) {
            referencedInScope = ReferencedInScope.UNDETERMINED
        }
        val variableName = psiElement as? ObjJVariableName
                ?: psiElement.getParentOfType( ObjJVariableName::class.java) ?: return false
        variableNameText = getQualifiedNameAsString(variableName)
        if (variableNameText != fqName) {
            return false
        }

        val commonContext = findCommonContext(variableName, myElement)
                ?: return referencedInScope == ReferencedInScope.UNDETERMINED
        //LOGGER.log(Level.INFO, "Checking qualified name <"+variableName.getText()+"> for isReferenceTo: "+fqName +" in container: "+referencedInScope.toString() + "; Common Context: <"+(commonContext != null ? commonContext.getNode().getElementType().toString() : "?")+">");
        if (referencedInScope == ReferencedInScope.CLASS) {
            val otherClassName = variableName.containingClassName
            return getAllInheritedClasses().contains(otherClassName)
        }

        if (referencedInScope == ReferencedInScope.FILE) {
            return myElement.containingFile.isEquivalentTo(variableName.containingFile)
        }
        if (referencedInScope == ReferencedInScope.IF) {
            return hasSharedContextOfType(commonContext, ObjJIfStatement::class.java)
        }

        if (referencedInScope == ReferencedInScope.ITERATION_HEADER) {
            return hasSharedContextOfType(commonContext, ObjJIterationStatement::class.java)
        }

        if (referencedInScope == ReferencedInScope.PREPROCESSOR_FUNCTION) {
            return hasSharedContextOfType(commonContext, ObjJPreprocessorDefineFunction::class.java)
        }

        if (referencedInScope == ReferencedInScope.METHOD) {
            return hasSharedContextOfType(commonContext, ObjJMethodDeclaration::class.java)
        }

        return if (referencedInScope == ReferencedInScope.TRY_CATCH) {
            hasSharedContextOfType(commonContext, ObjJTryStatement::class.java)
        } else referencedInScope == ReferencedInScope.UNDETERMINED
    }
*/

    override fun resolve(): PsiElement? {
        return resolve(true)
    }

    private fun resolve(nullIfSelfReferencing:Boolean) : PsiElement? {
        var variableName = ObjJVariableNameResolveUtil.getVariableDeclarationElement(myElement, false)
        if (variableName == null) {
            variableName = globalVariableNameElement
        }
        return if (nullIfSelfReferencing) {
            if (variableName != null && !variableName.isEquivalentTo(myElement)) variableName else null
        } else {
            //LOGGER.log(Level.INFO, "Variable \"${myElement.text}\" resolved? ${variableName != null}")
            variableName
        }
    }

    override fun getVariants(): Array<Any> {
        return arrayOf()
    }

    /**
     * Finds the containing scope for this variable and another
     * todo: Try to convert this isReferenceScope to the one implemented in ObjJVariableNameUtil.kt
     */
    private fun isReferencedInScope(psiElement: ObjJVariableName): ReferencedInScope {
        val commonContext = findCommonContext(psiElement, myElement)
        if (hasSharedContextOfType(commonContext, ObjJIfStatement::class.java)) {
            return ReferencedInScope.IF
        }

        if (hasSharedContextOfType(commonContext, ObjJIterationStatement::class.java)) {
            return ReferencedInScope.ITERATION_HEADER
        }

        if (hasSharedContextOfType(commonContext, ObjJTryStatement::class.java)) {
            return ReferencedInScope.TRY_CATCH
        }

        if (hasSharedContextOfType(commonContext, ObjJPreprocessorDefineFunction::class.java)) {
            return ReferencedInScope.PREPROCESSOR_FUNCTION
        }

        if (hasSharedContextOfType(commonContext, ObjJMethodDeclaration::class.java)) {
            return ReferencedInScope.METHOD
        }

        if (ObjJVariableNameUtil.isInstanceVarDeclaredInClassOrInheritance(myElement)) {
            return ReferencedInScope.CLASS
        }

        return if (myElement.containingFile.isEquivalentTo(psiElement.containingFile)) {
            ReferencedInScope.FILE
        } else ReferencedInScope.UNDETERMINED

    }
    companion object {

        private val LOGGER = Logger.getLogger(ObjJVariableReference::class.java.name)

        private fun <PsiT : PsiElement> hasSharedContextOfType(commonContext: PsiElement?, sharedContextClass: Class<PsiT>): Boolean {
            return sharedContextClass.isInstance(commonContext) || commonContext.getParentOfType( sharedContextClass) != null
        }
    }

}
