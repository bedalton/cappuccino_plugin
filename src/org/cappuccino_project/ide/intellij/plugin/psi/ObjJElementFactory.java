package org.cappuccino_project.ide.intellij.plugin.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;
import org.cappuccino_project.ide.intellij.plugin.annotator.IgnoreUtil;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJLanguage;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjJElementFactory {
    private static final Logger LOGGER = Logger.getLogger(ObjJElementFactory.class.getName());
    public static final String PLACEHOLDER_CLASS_NAME = "_XXX__";

    @Nullable
    public static ObjJSelector createSelector(Project project, String selector) {
        if (selector == null || selector.isEmpty()) {
            return null;
        }
        String scriptText = "@implementation "+PLACEHOLDER_CLASS_NAME+" \n - (void) "+selector+"{} @end";
        ObjJImplementationDeclaration implementationDeclaration = (ObjJImplementationDeclaration)createFileFromText(project, scriptText).getClassDeclarations().get(0);
        if (implementationDeclaration.getMethodDeclarationList().isEmpty()) {
            return null;
        } else if (implementationDeclaration.getMethodDeclarationList().get(0).getMethodHeader().getSelectorList().isEmpty()) {
            return null;
        }
        return implementationDeclaration.getMethodDeclarationList().get(0).getMethodHeader().getSelectorList().get(0);
    }

    public static ObjJVariableName createVariableName(Project project, String variableName) {
        String scriptText = "var "+variableName+";";
        ObjJFile file = createFileFromText(project, scriptText);
        ObjJBodyVariableAssignment variableAssignment = ObjJTreeUtil.getChildOfType(file, ObjJBodyVariableAssignment.class);
        assert variableAssignment != null;
        return variableAssignment.getQualifiedReferenceList().get(0).getVariableNameList().get(0);
    }

    @Nullable
    public static ObjJFunctionName createFunctionName(@NotNull Project project, @NotNull String functionName) {
        String scriptText = String.format("function %s(){}", functionName);
        LOGGER.log(Level.INFO, "Script text: <"+scriptText+">");
        ObjJFile file = createFileFromText(project, scriptText);
        ObjJFunctionDeclaration functionDeclaration = ObjJTreeUtil.getChildOfType(file, ObjJFunctionDeclaration.class);
        return functionDeclaration != null ? functionDeclaration.getFunctionName(): null;
    }

    public static PsiElement createSpace(Project project) {
        String scriptText = " ";
        ObjJFile file = createFileFromText(project, scriptText);
        return file.getFirstChild();
    }

    public static PsiErrorElement createSemiColonErrorElement(Project project) {
        String scriptText = "?*__ERR_SEMICOLON__*?";
        ObjJFile file = createFileFromText(project, scriptText);
        ObjJErrorSequence errorSequence = ObjJTreeUtil.getChildOfType(file, ObjJErrorSequence.class);
        PsiErrorElement errorElement = ObjJTreeUtil.getChildOfType(errorSequence, PsiErrorElement.class);
        if (errorElement == null) {
            List<String> childElementTypes = new ArrayList<>();
            for (PsiElement child : file.getChildren()) {
                childElementTypes.add(child.getNode().getElementType().toString());
            }
            LOGGER.log(Level.INFO, "createSemiColonErrorElement(Project project) Failed. No error element found. Found <"+ ArrayUtils.join(childElementTypes)+"> instead");
        }
        return errorElement;
    }

    @Nullable
    public static ObjJComment createIgnoreComment(Project project, IgnoreUtil.ElementType elementType) {
        String scriptText = "//ignore "+elementType.type;
        ObjJFile file = createFileFromText(project, scriptText);
        return PsiTreeUtil.getChildOfType(file, ObjJComment.class);
    }

    private static ObjJFile createFileFromText(Project project, String text) {
        return (ObjJFile) PsiFileFactory.getInstance(project).createFileFromText("dummy.j", ObjJLanguage.INSTANCE, text);
    }

    @NotNull
    private static ObjJMethodDeclaration createMethodDeclaration(@NotNull Project project, @NotNull ObjJMethodHeader methodHeader) {
        String script = "@implementation "+PLACEHOLDER_CLASS_NAME+" \n " +
                methodHeader.getText() + "\n" +
                "{" + "\n" +
                "    " + getDefaultReturnValueString(methodHeader.getMethodHeaderReturnTypeElement()) + "\n" +
                "}" + "\n" +
                "@end";
        ObjJFile file = createFileFromText(project, script);
        ObjJImplementationDeclaration implementationDeclaration = file.getChildOfType(ObjJImplementationDeclaration.class);
        assert implementationDeclaration != null;
        ObjJMethodDeclaration declaration = implementationDeclaration.getMethodDeclarationList().get(0);
        assert declaration != null;
        return declaration;
    }

    private static String getDefaultReturnValueString(@Nullable ObjJMethodHeaderReturnTypeElement returnTypeElement) {
        if (returnTypeElement == null || returnTypeElement.getFormalVariableType() == null) {
            return "";
        }
        String defaultValue = "nil";
        final ObjJFormalVariableType formalVariableType = returnTypeElement.getFormalVariableType();
        if (formalVariableType.getVarTypeBool() != null) {
            defaultValue = "NO";
        } else if (formalVariableType.getVarTypeId() != null || formalVariableType.getClassName() != null) {
            defaultValue = "Nil";
        }
        return "return " + defaultValue + ";";
    }

    @NotNull
    public static ObjJReturnStatement createReturnStatement(@NotNull Project project, String returnValue) {
        String scriptString =
                "function x() {\n" +
                    "return " + returnValue + ";" + "\n" +
                "}";
        ObjJFile file = createFileFromText(project, scriptString);
        ObjJFunctionDeclarationElement functionDeclarationElement = file.getChildOfType(ObjJFunctionDeclarationElement.class);
        assert functionDeclarationElement != null;
        ObjJReturnStatement returnStatement = functionDeclarationElement.getChildOfType(ObjJReturnStatement.class);
        assert returnStatement != null;
        return returnStatement;
    }

    public static PsiElement createCRLF(Project project) {
        final ObjJFile file = createFileFromText(project, "\n");
        return file.getFirstChild();
    }

}
