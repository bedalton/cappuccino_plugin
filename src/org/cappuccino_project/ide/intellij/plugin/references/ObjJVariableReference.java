package org.cappuccino_project.ide.intellij.plugin.references;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJFunctionsIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJNamedElement;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.*;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJFileUtil;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJInheritanceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sun.tools.tree.IfStatement;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.intellij.psi.util.PsiTreeUtil.findCommonContext;
import static org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJVariableNameUtil.getQualifiedNameAsString;

public class ObjJVariableReference extends PsiReferenceBase<ObjJVariableName> {

    private static final Logger LOGGER = Logger.getLogger(ObjJVariableReference.class.getName());
    private final String fqName;
    private List<String> allInheritedClasses;
    private ReferencedInScope referencedInScope;
    public ObjJVariableReference(
            @NotNull
                    ObjJVariableName element) {
        super(element, TextRange.create(0, element.getTextLength()));
        fqName = getQualifiedNameAsString(element);
        //LOGGER.log(Level.INFO, "Creating reference resolver for var <"+element.getName()+"> in file: <"+ObjJFileUtil.getContainingFileName(element.getContainingFile())+">");
    }

    private List<String> getAllInheritedClasses() {
        if (allInheritedClasses != null) {
            return allInheritedClasses;
        }
        allInheritedClasses = ObjJInheritanceUtil.getAllInheritedClasses(myElement.getContainingClassName(), myElement.getProject());
        return allInheritedClasses;
    }

    @Override
    @NotNull
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        PsiElement parent = getElement().getParent();
        ObjJVariableName newVariableName = ObjJElementFactory.createVariableName(myElement.getProject(), newElementName);
        parent.getNode().replaceChild(myElement.getNode(),newVariableName.getNode());
        return newVariableName;
    }

    @Override
    public boolean isReferenceTo(PsiElement psiElement) {
        String variableNameText = psiElement.getText();
        if (!variableNameText.equals(myElement.getText())) {
            return false;
        }
        if (psiElement instanceof ObjJClassName) {
            return true;
        }
        if (referencedInScope == null) {
            PsiElement referencedElement = resolve();
            if (referencedElement == null) {
                referencedElement = myElement;
            }
            if (referencedElement instanceof ObjJVariableName) {
                referencedInScope = isReferencedInScope((ObjJVariableName)referencedElement);
            } else {

                if (myElement.isIn(ObjJMethodHeaderDeclaration.class)) {
                    referencedInScope = ReferencedInScope.METHOD;
                }

                if (myElement.isIn(ObjJFormalParameterArg.class)) {
                    referencedInScope = ReferencedInScope.FUNCTION;
                }

                if (myElement.isIn(ObjJInstanceVariableDeclaration.class)) {
                    referencedInScope = ReferencedInScope.CLASS;
                }

                if (myElement.isIn(ObjJBodyVariableAssignment.class)) {
                    ObjJBodyVariableAssignment bodyVariableAssignment = myElement.getParentOfType(ObjJBodyVariableAssignment.class);
                    assert bodyVariableAssignment != null;
                    if (bodyVariableAssignment.getVarModifier() == null) {
                        if (bodyVariableAssignment.getParent() instanceof IfStatement) {
                            referencedInScope = ReferencedInScope.IF;
                        }

                        if (bodyVariableAssignment.getParent() instanceof ObjJIterationStatement) {
                            referencedInScope = ReferencedInScope.ITERATION_HEADER;
                        }
                    }
                }
            }
        }
        if (referencedInScope == null) {
            referencedInScope = ReferencedInScope.UNDETERMINED;
        }
        ObjJVariableName variableName = psiElement instanceof ObjJVariableName ? (ObjJVariableName)psiElement : ObjJTreeUtil.getParentOfType(psiElement, ObjJVariableName.class);
        if (variableName == null) {
            return false;
        }
        variableNameText = getQualifiedNameAsString(variableName);
        if (!variableNameText.equals(fqName)) {
            return false;
        }

        PsiElement commonContext = findCommonContext(variableName, myElement);
        //LOGGER.log(Level.INFO, "Checking qualified name <"+variableName.getText()+"> for isReferenceTo: "+fqName +" in container: "+referencedInScope.toString() + "; Common Context: <"+(commonContext != null ? commonContext.getNode().getElementType().toString() : "?")+">");
        if (commonContext == null) {
            return referencedInScope == ReferencedInScope.UNDETERMINED;
        }
        if (referencedInScope == ReferencedInScope.CLASS) {
            String otherClassName = variableName.getContainingClassName();
            return getAllInheritedClasses().contains(otherClassName);
        }

        if (referencedInScope == ReferencedInScope.FILE) {
            return myElement.getContainingFile().isEquivalentTo(variableName.getContainingFile());
        }
        if (referencedInScope == ReferencedInScope.IF) {
            return hasSharedContextOfType(commonContext, ObjJIfStatement.class);
        }

        if (referencedInScope == ReferencedInScope.ITERATION_HEADER) {
            return hasSharedContextOfType(commonContext, ObjJIterationStatement.class);
        }

        if (referencedInScope == ReferencedInScope.PREPROCESSOR_FUNCTION) {
            return hasSharedContextOfType(commonContext, ObjJPreprocessorDefineFunction.class);
        }

        if (referencedInScope == ReferencedInScope.METHOD) {
            return hasSharedContextOfType(commonContext, ObjJMethodDeclaration.class);
        }

        if (referencedInScope == ReferencedInScope.TRY_CATCH) {
            return hasSharedContextOfType(commonContext, ObjJTryStatement.class);
        }
        return referencedInScope == ReferencedInScope.UNDETERMINED;
    }

    private static <PsiT extends PsiElement> boolean hasSharedContextOfType(PsiElement commonContext, Class<PsiT> sharedContextClass) {
        return sharedContextClass.isInstance(commonContext) || ObjJTreeUtil.getParentOfType(commonContext, sharedContextClass) != null;
    }


    @Nullable
    @Override
    public PsiElement resolve() {
        //LOGGER.log(Level.INFO, "Resolving var with name: <" + myElement.getText() + ">");
        PsiElement variableName = ObjJVariableNameResolveUtil.getVariableDeclarationElement(myElement, false);
        if (variableName == null) {
            variableName = getGlobalVariableNameElement();
        }
        return variableName != null && !variableName.isEquivalentTo(myElement) ? variableName : null;
    }

    private PsiElement getGlobalVariableNameElement() {
        if (DumbService.isDumb(myElement.getProject())) {
            return null;
        }
        ObjJFile file = myElement.getContainingObjJFile();
        List<String> imports = file != null ? file.getImportStrings() : null;
        List<ObjJGlobalVariableDeclaration> globalVariableDeclarations = ObjJGlobalVariableNamesIndex.getInstance().get(myElement.getText(), myElement.getProject());
        if (!globalVariableDeclarations.isEmpty()) {
            if (imports == null) {

                return globalVariableDeclarations.get(0).getVariableName();
            }
            for (ObjJGlobalVariableDeclaration declaration : globalVariableDeclarations) {
                if (imports.contains(ObjJFileUtil.getContainingFileName(declaration.getContainingFile()))) {
                    return declaration.getVariableName();
                }
            }
        }
        List<ObjJFunctionDeclarationElement> functionDeclarationElements = ObjJFunctionsIndex.getInstance().get(myElement.getText(), myElement.getProject());
        if (!functionDeclarationElements.isEmpty()) {
            ObjJNamedElement namedElement = functionDeclarationElements.get(0).getFunctionNameNode();
            if (namedElement == null) {
                for (ObjJFunctionDeclarationElement declarationElement : functionDeclarationElements) {
                    namedElement = declarationElement.getFunctionNameNode();
                    if (namedElement != null) {
                        break;
                    }
                }
            }
            return namedElement != null && !namedElement.isEquivalentTo(myElement) ? namedElement : null;
        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    private ReferencedInScope isReferencedInScope(ObjJVariableName psiElement) {
        PsiElement commonContext = findCommonContext(psiElement, myElement);
        if (hasSharedContextOfType(commonContext, ObjJIfStatement.class)) {
            return ReferencedInScope.IF;
        }

        if (hasSharedContextOfType(commonContext, ObjJIterationStatement.class)) {
            return ReferencedInScope.ITERATION_HEADER;
        }

        if (hasSharedContextOfType(commonContext, ObjJTryStatement.class)) {
            return ReferencedInScope.TRY_CATCH;
        }

        if (hasSharedContextOfType(commonContext, ObjJPreprocessorDefineFunction.class)) {
            return ReferencedInScope.PREPROCESSOR_FUNCTION;
        }

        if (hasSharedContextOfType(commonContext, ObjJMethodDeclaration.class)) {
            return ReferencedInScope.METHOD;
        }

        if (ObjJVariableNameUtil.isInstanceVarDeclaredInClassOrInheritance(myElement)) {
            return ReferencedInScope.CLASS;
        }

        if (myElement.getContainingFile().isEquivalentTo(psiElement.getContainingFile())) {
            return ReferencedInScope.FILE;
        }

        return ReferencedInScope.UNDETERMINED;
    }

    private enum ReferencedInScope {
        UNDETERMINED,
        CLASS,
        FILE,
        FUNCTION,
        IF,
        ITERATION_HEADER,
        METHOD,
        PREPROCESSOR_FUNCTION,
        TRY_CATCH,
    }

}
