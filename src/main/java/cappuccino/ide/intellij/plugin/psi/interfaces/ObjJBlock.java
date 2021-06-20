package cappuccino.ide.intellij.plugin.psi.interfaces;

import cappuccino.ide.intellij.plugin.psi.*;
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ObjJBlock extends ObjJCompositeElement, ObjJHasBlockStatement {

    @Nullable
    PsiElement getOpenBrace();

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
    default PsiElement getCloseBrace() {
        return ObjJPsiImplUtil.getCloseBrace(this);
    }

    default ObjJBlock getBlock() {
        return this;
    }

}
