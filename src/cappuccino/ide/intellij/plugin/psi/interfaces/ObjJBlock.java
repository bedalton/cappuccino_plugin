package cappuccino.ide.intellij.plugin.psi.interfaces;

import cappuccino.ide.intellij.plugin.psi.*;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ObjJBlock extends ObjJCompositeElement {

    @NotNull
    List<cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock> getBlockList();

    @NotNull
    List<ObjJBodyVariableAssignment> getBodyVariableAssignmentList();

    @NotNull
    List<ObjJBreakStatement> getBreakStatementList();

    @NotNull
    List<ObjJComment> getCommentList();

    @NotNull
    List<ObjJDebuggerStatement> getDebuggerStatementList();

    @NotNull
    List<ObjJDeleteStatement> getDeleteStatementList();

    @NotNull
    List<ObjJExpr> getExprList();

    @NotNull
    List<ObjJFunctionDeclaration> getFunctionDeclarationList();

    @NotNull
    List<ObjJIfStatement> getIfStatementList();

    @NotNull
    List<ObjJIncludeFile> getIncludeFileList();

    @NotNull
    List<ObjJIncludeFramework> getIncludeFrameworkList();

    @NotNull
    List<ObjJIterationStatement> getIterationStatementList();

    @NotNull
    List<ObjJPreprocessorIfStatement> getPreprocessorIfStatementList();

    @NotNull
    List<ObjJReturnStatement> getReturnStatementList();

    @NotNull
    List<ObjJSwitchStatement> getSwitchStatementList();

    @NotNull
    List<ObjJThrowStatement> getThrowStatementList();

    @NotNull
    List<ObjJTryStatement> getTryStatementList();

    @Nullable
    PsiElement getOpenBrace();
}
