package org.cappuccino_project.ide.intellij.plugin.annotator;

import com.intellij.lang.ASTNode;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.cappuccino_project.ide.intellij.plugin.contributor.ObjJKeywordsList;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJClassDeclarationsIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJFunctionsIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJGlobalVariableNamesIndex;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJInstanceVariablesByNameIndex;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.*;
import org.cappuccino_project.ide.intellij.plugin.references.ObjJVariableReference;
import org.cappuccino_project.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.*;
import org.cappuccino_project.ide.intellij.plugin.settings.ObjJVariableAnnotatorSettings;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJInheritanceUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class used to annotate variable references
 */
@SuppressWarnings("Duplicates")
class ObjJVariableAnnotatorUtil {

    private static final Logger LOGGER = Logger.getLogger(ObjJVariableAnnotatorUtil.class.getName());
    private static final String OVERSHADOWS_VARIABLE_STRING_FORMAT = "Variable overshadows existing variable in %s";
    private static final String OVERSHADOWS_FUNCTION_NAME_STRING_FORMAT = "Variable overshadows function with name %s";
    private static final String OVERSHADOWS_METHOD_HEADER_VARIABLE = "Variable overshadows method variable";
    private static final List<String> STATIC_VAR_NAMES = Arrays.asList("this", "Array", "ObjectiveJ", "arguments", "document", "window");
    //private static final HashMap<PsiFile, List<Integer>> checked = new HashMap<>();

    /**
     * Annotate variable name element
     * @param variableName variable name element
     * @param annotationHolder annotation holder
     */
    static void annotateVariable(
            @Nullable
            final ObjJVariableName variableName,
            @NotNull
            final AnnotationHolder annotationHolder) {
        if (variableName == null || variableName.getText().isEmpty()) {
            LOGGER.log(Level.INFO, "Var Name Is Null for annotator.");
            return;
        }

        ASTNode prevNode = ObjJTreeUtil.getPreviousNonEmptyNode(variableName, true);
        if (prevNode != null && prevNode.getElementType() == ObjJTypes.ObjJ_DOT) {
            return;
        }

        //LOGGER.log(Level.INFO, "Checking variableName <"+variableName.getText()+">");
        if (DumbService.isDumb(variableName.getProject())) {
            DumbService.getInstance(variableName.getProject()).smartInvokeLater(() -> annotateVariable(variableName, annotationHolder));
            return;
        }
        annotateIfVariableOvershadows(variableName, annotationHolder);
        annotateIfVariableIsNotDeclaredBeforeUse(variableName, annotationHolder);
    }

    private static void annotateIfVariableIsNotDeclaredBeforeUse(@NotNull ObjJVariableName variableName, @NotNull final AnnotationHolder annotationHolder) {

        if (ObjJTreeUtil.getParentOfType(variableName, ObjJInstanceVariableList.class) != null) {
            switch (variableName.getText()) {
                case "super":
                case "this":
                case "self":
                    annotationHolder.createErrorAnnotation(variableName, "Using reserved variable name");
            }
            return;
        }

        if (variableName.getParent() instanceof ObjJQualifiedReference) {
            variableName = ((ObjJQualifiedReference) variableName.getParent()).getPrimaryVar();
        }
        if (variableName == null) {
            return;
        }


        if (STATIC_VAR_NAMES.contains(variableName.getText())) {
            annotateStaticVariableNameReference(variableName, annotationHolder);
            return;
        }

        if (isItselfAVariableDeclaration(variableName)) {
            return;
        }
        final Project project = variableName.getProject();
        if (DumbService.isDumb(project)) {
            LOGGER.log(Level.WARNING, "annotating variable should have been skipped if in dumb mode");
            return;
        }

        if (!ObjJClassDeclarationsIndex.getInstance().get(variableName.getText(), variableName.getProject()).isEmpty()) {
            return;
        }

        if (isDeclaredInEnclosingScopesHeader(variableName)) {
            return;
        }

        if (isVariableDeclaredBeforeUse(variableName)) {
            //LOGGER.log(Level.INFO, "Variable is <" + variableName.getText() + "> declared before use.");
            return;
        }


        PsiElement tempElement = ObjJTreeUtil.getNextNonEmptySibling(variableName, true);
        if (tempElement != null && tempElement.getText().equals(".")) {
            tempElement = ObjJTreeUtil.getNextNonEmptySibling(tempElement, true);
            if (tempElement instanceof ObjJFunctionCall) {
                ObjJFunctionCall functionCall = (ObjJFunctionCall)tempElement;
                if (functionCall.getFunctionName() != null && functionCall.getFunctionName().getText().equals("call")) {
                    if (ObjJFunctionsIndex.getInstance().get(variableName.getName(), variableName.getProject()).isEmpty()) {
                        annotationHolder.createWarningAnnotation(variableName, "Failed to find function with name <"+variableName.getName()+">");
                    }
                    return;
                }
            }
        }
        List<ObjJGlobalVariableDeclaration> declarations = ObjJGlobalVariableNamesIndex.getInstance().get(variableName.getText(), variableName.getProject());
        if (!declarations.isEmpty()) {
            annotationHolder.createInfoAnnotation(variableName, "References global variable in file <" + (declarations.get(0).getFileName() != null ? declarations.get(0).getFileName() : "UNDEFINED" + ">") + ">");
            return;
        }
        if (variableName.getText().substring(0,1).equals(variableName.getText().substring(0,1).toUpperCase())) {
            //annotationHolder.createWeakWarningAnnotation(variableName,"Variable may reference javascript class");
            return;
        }

        if (variableName.hasText("self") || variableName.hasText("super")) {
            if (ObjJMethodCallPsiUtil.isUniversalMethodCaller(variableName.getContainingClassName())) {
                annotationHolder.createErrorAnnotation(variableName, variableName.getText()+ " used outside of class");
            }
            return;
        }
        //LOGGER.log(Level.INFO, "Var <" + variableName.getText() + "> is undeclared.");
        annotationHolder.createWarningAnnotation(variableName.getTextRange(), "Variable may not have been declared before use");

    }

    private static boolean isVariableDeclaredBeforeUse(ObjJVariableName variableName) {
        if (ObjJKeywordsList.keywords.contains(variableName.getText())) {
            return true;
        }
        List<ObjJVariableName> precedingVariableNameReferences = ObjJVariableNameUtil.getMatchingPrecedingVariableNameElements(variableName, 0);
        return !precedingVariableNameReferences.isEmpty() || !ObjJFunctionsIndex.getInstance().get(variableName.getText(), variableName.getProject()).isEmpty() || new ObjJVariableReference(variableName).resolve() != null;
    }

    private static void annotateStaticVariableNameReference(@NotNull ObjJVariableName variableName, @NotNull AnnotationHolder annotationHolder) {
        String variableNameString = variableName.getText();
        switch (variableNameString) {
            case "this":
                if (ObjJTreeUtil.getParentOfType(variableName, ObjJBlock.class) == null) {
                    annotationHolder.createWarningAnnotation(variableName, "Possible misuse of 'this' outside of block");
                }
                break;
        }
    }

    private static boolean isDeclaredInEnclosingScopesHeader(@NotNull ObjJVariableName variableName) {
        return ObjJVariableNameUtil.isInstanceVarDeclaredInClassOrInheritance(variableName) ||
                isDeclaredInContainingMethodHeader(variableName) ||
                isDeclaredInFunctionScope(variableName) ||
                !ObjJVariableNameUtil.getMatchingPrecedingVariableNameElements(variableName, 0).isEmpty();
    }

    private static boolean isDeclaredInContainingMethodHeader(@NotNull ObjJVariableName variableName) {
        ObjJMethodDeclaration methodDeclaration = ObjJTreeUtil.getParentOfType(variableName, ObjJMethodDeclaration.class);
        return methodDeclaration != null && ObjJMethodPsiUtils.getHeaderVariableNameMatching(methodDeclaration.getMethodHeader(), variableName.getText()) != null;
    }

    private static boolean isDeclaredInFunctionScope(ObjJVariableName variableName) {
        ObjJFunctionDeclarationElement functionDeclarationElement = ObjJTreeUtil.getParentOfType(variableName, ObjJFunctionDeclarationElement.class);
        if (functionDeclarationElement != null) {
            for (Object ob : functionDeclarationElement.getFormalParameterArgList()) {
                if (!(ob instanceof ObjJFormalParameterArg)) {
                    continue;
                }
                ObjJFormalParameterArg arg = ((ObjJFormalParameterArg)ob);
                if (arg.getVariableName().getText().equals(variableName.getText())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isItselfAVariableDeclaration(ObjJVariableName variableName) {
        //If variable name is itself an instance variable
        if (variableName.getParent() instanceof  ObjJInstanceVariableDeclaration) {
            return true;
        }
        //If variable name element is itself a method header declaration variable
        if (ObjJTreeUtil.getParentOfType(variableName, ObjJMethodHeaderDeclaration.class) != null) {
            return true;
        }

        if (variableName.getParent() instanceof ObjJGlobalVariableDeclaration) {
            return true;
        }

        //If variable name is itself an function variable
        if (variableName.getParent() instanceof ObjJFormalParameterArg) {
            return true;
        }

        //If variable name itself is declared in catch header in try/catch block
        if (variableName.getParent() instanceof ObjJCatchProduction) {
            return true;
        }
        //If variable name itself a javascript object property name
        if (variableName.getParent() instanceof ObjJPropertyAssignment) {
            return true;
        }

        if (variableName.getParent() instanceof ObjJInExpr) {
            return true;
        }

        if (variableName.getParentOfType(ObjJPreprocessorDefineFunction.class) != null) {
            return true;
        }

        if (variableName.getParent() instanceof ObjJGlobal) {
            return true;
        }

        ObjJQualifiedReference reference = ObjJTreeUtil.getParentOfType(variableName, ObjJQualifiedReference.class);
        if (reference == null) {
            return false;
        }

        if (reference.getParent() instanceof ObjJBodyVariableAssignment) {
            return ((ObjJBodyVariableAssignment)reference.getParent()).getVarModifier() != null;
        }

        if (reference.getParent() instanceof ObjJIterationStatement) {
            return true;
        }

        ObjJBodyVariableAssignment assignment = null;
        if (reference.getParent() instanceof ObjJVariableDeclaration) {
            ObjJVariableDeclaration variableDeclaration = ((ObjJVariableDeclaration) reference.getParent());
            if (variableDeclaration.getParent() instanceof ObjJIterationStatement && ObjJTreeUtil.siblingOfTypeOccursAtLeastOnceBefore(variableDeclaration, ObjJVarModifier.class)) {
                return true;
            } else if (variableDeclaration.getParent() instanceof ObjJGlobalVariableDeclaration) {
                return true;
            } else {
                //LOGGER.log(Level.INFO, "Variable declaration has a parent of type: <"+variableDeclaration.getParent().getNode().getElementType().toString()+">");
            }
            assignment = variableDeclaration.getParent() instanceof ObjJBodyVariableAssignment ? ((ObjJBodyVariableAssignment) variableDeclaration.getParent()) : null;
        }
        return assignment != null && assignment.getVarModifier() != null;
    }

    /**
     * Annotates variable if it overshadows a variable in enclosing scope
     * @param variableName variable to possibly annotate
     * @param annotationHolder annotation holder
     */
    private static void annotateIfVariableOvershadows(@NotNull final ObjJVariableName variableName, @NotNull AnnotationHolder annotationHolder) {
        if (variableName.getText().isEmpty() || !isItselfAVariableDeclaration(variableName)) {
            return;
        }
        ObjJInstanceVariableList variableList = ObjJTreeUtil.getParentOfType(variableName, ObjJInstanceVariableList.class);
        if (variableList != null) {
            ObjJInstanceVariableDeclaration thisInstanceVariable = variableName.getParentOfType(ObjJInstanceVariableDeclaration.class);
            int startOffset = thisInstanceVariable != null ? thisInstanceVariable.getTextRange().getStartOffset() : 0;
            String variableNameString = variableName.getText();
            for (ObjJInstanceVariableDeclaration instanceVariableDeclaration : variableList.getInstanceVariableDeclarationList()) {
                if (instanceVariableDeclaration.getVariableName() == null) {
                    continue;
                }
                if (instanceVariableDeclaration.getVariableName().hasText(variableNameString) && instanceVariableDeclaration.getTextRange().getStartOffset() < startOffset) {
                    annotationHolder.createErrorAnnotation(variableName, "Variable with name already declared.");
                    return;
                }
            }
        }
        if (isBodyVariableAssignment(variableName)) {
            annotateIfOvershadowsBlocks(variableName, annotationHolder);
            annotateIfOvershadowsMethodVariable(variableName, annotationHolder);
            annotateVariableIfOvershadowInstanceVariable(variableName, annotationHolder);
            annotateVariableIfOvershadowsFileVars(variableName, annotationHolder);
        } else if (isInstanceVariable(variableName)) {
            annotateVariableIfOvershadowsFileVars(variableName, annotationHolder);
        }
    }

    /**
     * Checks whether this variable is a body variable assignment declaration
     * @param variableName variable name element
     * @return <code>true</code> if variable name element is part of a variable declaration
     */
    private static boolean isBodyVariableAssignment(ObjJVariableName variableName) {
        ObjJBodyVariableAssignment bodyVariableAssignment = ObjJTreeUtil.getParentOfType(variableName, ObjJBodyVariableAssignment.class);
        return bodyVariableAssignment != null && bodyVariableAssignment.getVarModifier() != null;
    }

    /**
     * Checks whether this variable name is part of a instance variable declaration
     * @param variableName variable name element
     * @return <code>true</code> if variable name is part of instance variable declaration
     */
    private static boolean isInstanceVariable(ObjJVariableName variableName) {
        return ObjJTreeUtil.getParentOfType(variableName, ObjJInstanceVariableDeclaration.class) != null;
    }

    private static void annotateIfOvershadowsMethodVariable(@NotNull ObjJVariableName variableName, @NotNull AnnotationHolder annotationHolder) {
        //Variable is defined in header itself
        if (variableName.getParentOfType(ObjJMethodHeader.class) != null) {
            return;
        }
        //Check if method is actually in a method declaration
        ObjJMethodDeclaration methodDeclaration = variableName.getParentOfType(ObjJMethodDeclaration.class);
        if (methodDeclaration == null) {
            return;
        }

        //Check if variable overshadows variable defined in method header
        if (ObjJMethodPsiUtils.getHeaderVariableNameMatching(methodDeclaration.getMethodHeader(), variableName.getText()) != null) {
            createAnnotation(ObjJVariableAnnotatorSettings.OVERSHADOWS_METHOD_VARIABLE_SETTING.getValue(), variableName, OVERSHADOWS_METHOD_HEADER_VARIABLE, annotationHolder);
        }
    }

    /**
     * Annotates a body variable assignment if it overshadows an instance variable
     * @param variableName variable name element
     * @param annotationHolder annotation holder
     */
    private static void annotateVariableIfOvershadowInstanceVariable(@NotNull ObjJVariableName variableName, @NotNull AnnotationHolder annotationHolder) {
        final Project project = variableName.getProject();
        ObjJClassDeclarationElement classDeclarationElement = ObjJTreeUtil.getParentOfType(variableName, ObjJClassDeclarationElement.class);
        if (classDeclarationElement == null) {
            return;
        }
        final String variableContainingClass = classDeclarationElement.getClassNameString();
        String scope = null;
        List<String> inheritedClassNames = ObjJInheritanceUtil.getAllInheritedClasses(classDeclarationElement.getClassNameString(), classDeclarationElement.getProject());
        final AnnotationLevel annotationLevel = ObjJVariableAnnotatorSettings.OVERSHADOWS_INSTANCE_VARIABLE_SETTING.getValue();
        for (ObjJInstanceVariableDeclaration instanceVariableDeclaration : ObjJInstanceVariablesByNameIndex.getInstance().get(classDeclarationElement.getClassNameString(), project)) {
            ProgressIndicatorProvider.checkCanceled();
            final String instanceVarContainingClass = instanceVariableDeclaration.getContainingClassName();
            if (instanceVarContainingClass.equals(variableContainingClass)) {
                scope = "containing class <" + classDeclarationElement.getClassName() + ">";
                break;
            }
            if (inheritedClassNames.contains(instanceVarContainingClass)) {
                scope = "parent class <" + instanceVarContainingClass + ">";
                break;
            }
        }
        if (scope != null) {
            createAnnotation(annotationLevel, variableName, String.format(OVERSHADOWS_VARIABLE_STRING_FORMAT, scope), annotationHolder);
        }
    }

    private static void annotateIfOvershadowsBlocks(@NotNull ObjJVariableName variableName, @NotNull AnnotationHolder annotationHolder) {
        List<ObjJBodyVariableAssignment> bodyVariableAssignments = ObjJBlockPsiUtil.getParentBlockChildrenOfType(variableName,ObjJBodyVariableAssignment.class,  true);
        if (bodyVariableAssignments.isEmpty()) {
            return;
        }
        final int offset = variableName.getTextRange().getStartOffset();
        final String variableNameString = variableName.getText();
        final AnnotationLevel annotationLevel = ObjJVariableAnnotatorSettings.OVERSHADOWS_BLOCK_VARIABLE_SETTING.getValue();
        for (ObjJBodyVariableAssignment bodyVariableAssignment : bodyVariableAssignments) {
            if (isDeclaredInBodyVariableAssignment(bodyVariableAssignment, variableNameString, offset)) {
                createAnnotation(annotationLevel, variableName, "Variable overshadows variable in enclosing block", annotationHolder);
                return;
            }
        }
    }

    private static boolean isDeclaredInBodyVariableAssignment(@NotNull ObjJBodyVariableAssignment variableAssignment, @NotNull final String variableNameString, final int offset) {
        if (variableAssignment.getVarModifier() == null) {
            return false;
        }
        List<ObjJQualifiedReference> qualifiedReferences = variableAssignment.getQualifiedReferenceList();
        List<ObjJVariableName> varNames = new ArrayList<>();
        for (ObjJVariableDeclaration declaration : variableAssignment.getVariableDeclarationList()) {
            qualifiedReferences.addAll(declaration.getQualifiedReferenceList());
        }
        for (ObjJQualifiedReference qualifiedReference : qualifiedReferences) {
            varNames.add(qualifiedReference.getPrimaryVar());
        }
        return ObjJVariableNameUtil.getFirstMatchOrNull(varNames, (varName) -> varName.getText().equals(variableNameString) && offset > varName.getTextRange().getStartOffset()) != null;
    }

    /**
     * Annotes variable if it overshadows any file scoped variables or function names
     * @param variableName variable name
     * @param annotationHolder annotation holder
     */
    private static void annotateVariableIfOvershadowsFileVars(@NotNull final ObjJVariableName variableName, @NotNull final AnnotationHolder annotationHolder) {
        final PsiFile file = variableName.getContainingFile();
        final ObjJVariableName reference = ObjJVariableNameUtil.getFirstMatchOrNull(ObjJVariableNameUtil.getAllFileScopedVariables(file, 0), (var) -> variableName.getText().equals(var.getText()));
        if (reference != null && reference != variableName) {
            annotationHolder.createWarningAnnotation(variableName, String.format(OVERSHADOWS_VARIABLE_STRING_FORMAT, "file scope"));
            return;
        }
        final AnnotationLevel annotationLevel = ObjJVariableAnnotatorSettings.OVERSHADOWS_FILE_VARIABLE_SETTING.getValue();
        for (ObjJFunctionDeclarationElement declarationElement : ObjJFunctionsIndex.getInstance().get(variableName.getText(), variableName.getProject())) {
            ProgressIndicatorProvider.checkCanceled();
            if (declarationElement.getContainingFile().isEquivalentTo(file) && declarationElement.getFunctionNameNode() != null && variableName.getTextRange().getStartOffset() > declarationElement.getFunctionNameNode().getTextRange().getStartOffset()) {
                //createAnnotation(annotationLevel, variableName, String.format(OVERSHADOWS_FUNCTION_NAME_STRING_FORMAT, variableName.getText()), annotationHolder);
            }

        }
    }

    private static void createAnnotation(@NotNull AnnotationLevel level, @NotNull PsiElement target, @NotNull String message, @NotNull AnnotationHolder annotationHolder) {
        switch(level) {
            case ERROR:
                annotationHolder.createErrorAnnotation(target, message);
                return;
            case WARNING:
                annotationHolder.createWarningAnnotation(target, message);
                return;
            case WEAK_WARNING:
                annotationHolder.createWeakWarningAnnotation(target, message);
        }
    }

    @SuppressWarnings("unused")
    private static void createAnnotation(@NotNull AnnotationLevel level, @NotNull TextRange target, @NotNull String message, @NotNull AnnotationHolder annotationHolder) {
        switch(level) {
            case ERROR:
                annotationHolder.createErrorAnnotation(target, message);
                return;
            case WARNING:
                annotationHolder.createWarningAnnotation(target, message);
                return;
            case WEAK_WARNING:
                annotationHolder.createWeakWarningAnnotation(target, message);
        }
    }
}
