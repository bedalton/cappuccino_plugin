package org.cappuccino_project.ide.intellij.plugin.references

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.util.IncorrectOperationException
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJFunctionsIndex
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile
import org.cappuccino_project.ide.intellij.plugin.psi.*
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJNamedElement
import org.cappuccino_project.ide.intellij.plugin.psi.utils.*
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJFileUtil
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJInheritanceUtil
import sun.tools.tree.IfStatement
import java.util.logging.Level
import java.util.logging.Logger

import com.intellij.psi.util.PsiTreeUtil.findCommonContext
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJVariableNameUtil.getQualifiedNameAsString

class ObjJVariableReference(
        element: ObjJVariableName) : PsiReferenceBase<ObjJVariableName>(element, TextRange.create(0, element.textLength)) {
    private val fqName: String
    private var allInheritedClasses: List<String>? = null
    private var referencedInScope: ReferencedInScope? = null

    private val globalVariableNameElement: PsiElement?
        get() {
            if (DumbService.isDumb(myElement.project)) {
                return null
            }
            val file = myElement.containingObjJFile
            val imports = file?.importStrings
            val globalVariableDeclarations = ObjJGlobalVariableNamesIndex.instance.get(myElement.text, myElement.project)
            if (!globalVariableDeclarations.isEmpty()) {
                if (imports == null) {

                    return globalVariableDeclarations.get(0).variableName
                }
                for (declaration in globalVariableDeclarations) {
                    if (imports.contains(ObjJFileUtil.getContainingFileName(declaration.getContainingFile()))) {
                        return declaration.variableName
                    }
                }
            }
            val functionDeclarationElements = ObjJFunctionsIndex.instance.get(myElement.text, myElement.project)
            if (!functionDeclarationElements.isEmpty()) {
                var namedElement = functionDeclarationElements.get(0).functionNameNode
                if (namedElement == null) {
                    for (declarationElement in functionDeclarationElements) {
                        namedElement = declarationElement.functionNameNode
                        if (namedElement != null) {
                            break
                        }
                    }
                }
                return if (namedElement != null && !namedElement!!.isEquivalentTo(myElement)) namedElement else null
            }
            return null
        }

    init {
        fqName = getQualifiedNameAsString(element)
        //LOGGER.log(Level.INFO, "Creating reference resolver for var <"+element.getName()+"> in file: <"+ObjJFileUtil.getContainingFileName(element.getContainingFile())+">");
    }

    private fun getAllInheritedClasses(): List<String> {
        if (allInheritedClasses != null) {
            return allInheritedClasses
        }
        allInheritedClasses = ObjJInheritanceUtil.getAllInheritedClasses(myElement.containingClassName, myElement.project)
        return allInheritedClasses
    }

    @Throws(IncorrectOperationException::class)
    override fun handleElementRename(newElementName: String): PsiElement {
        val parent = element.parent
        val newVariableName = ObjJElementFactory.createVariableName(myElement.project, newElementName)
        parent.node.replaceChild(myElement.node, newVariableName.node)
        return newVariableName
    }

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
                referencedInScope = isReferencedInScope(referencedElement as ObjJVariableName?)
            } else {

                if (myElement.isIn(ObjJMethodHeaderDeclaration<*>::class.java)) {
                    referencedInScope = ReferencedInScope.METHOD
                }

                if (myElement.isIn(ObjJFormalParameterArg::class.java)) {
                    referencedInScope = ReferencedInScope.FUNCTION
                }

                if (myElement.isIn(ObjJInstanceVariableDeclaration::class.java)) {
                    referencedInScope = ReferencedInScope.CLASS
                }

                if (myElement.isIn(ObjJBodyVariableAssignment::class.java)) {
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
                ?: ObjJTreeUtil.getParentOfType(psiElement, ObjJVariableName::class.java) ?: return false
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


    override fun resolve(): PsiElement? {
        //LOGGER.log(Level.INFO, "Resolving var with name: <" + myElement.getText() + ">");
        var variableName = ObjJVariableNameResolveUtil.getVariableDeclarationElement(myElement, false)
        if (variableName == null) {
            variableName = globalVariableNameElement
        }
        return if (variableName != null && !variableName.isEquivalentTo(myElement)) variableName else null
    }

    override fun getVariants(): Array<Any> {
        return arrayOfNulls(0)
    }

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

    private enum class ReferencedInScope {
        UNDETERMINED,
        CLASS,
        FILE,
        FUNCTION,
        IF,
        ITERATION_HEADER,
        METHOD,
        PREPROCESSOR_FUNCTION,
        TRY_CATCH
    }

    companion object {

        private val LOGGER = Logger.getLogger(ObjJVariableReference::class.java.name)

        private fun <PsiT : PsiElement> hasSharedContextOfType(commonContext: PsiElement?, sharedContextClass: Class<PsiT>): Boolean {
            return sharedContextClass.isInstance(commonContext) || ObjJTreeUtil.getParentOfType(commonContext, sharedContextClass) != null
        }
    }

}
