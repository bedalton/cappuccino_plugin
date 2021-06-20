package cappuccino.ide.intellij.plugin.psi.impl;

import cappuccino.ide.intellij.plugin.psi.*;
import cappuccino.ide.intellij.plugin.psi.interfaces.*;
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.ObjJ_CLOSE_BRACE;
import static cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.ObjJ_OPEN_BRACE;

public class ObjJBlockMixin extends ObjJCompositeElementImpl implements ObjJBlock, ObjJHasBlockStatement, ObjJHasBlockStatements, ObjJCompositeElement, ObjJChildrenRequireSemiColons, ObjJHasIgnoreStatements {

    ObjJBlockMixin(
            @NotNull
                    ASTNode node) {
        super(node);
    }

    @Override
    @NotNull
    public List<ObjJBodyVariableAssignment> getBodyVariableAssignmentList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJBodyVariableAssignment.class);
    }

    @Override
    @NotNull
    public List<ObjJBreakStatement> getBreakStatementList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJBreakStatement.class);
    }

    @Override
    @NotNull
    public List<ObjJComment> getCommentList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJComment.class);
    }

    @Override
    @NotNull
    public List<ObjJDebuggerStatement> getDebuggerStatementList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJDebuggerStatement.class);
    }

    @Override
    @NotNull
    public List<ObjJDeleteStatement> getDeleteStatementList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJDeleteStatement.class);
    }

    @Override
    @NotNull
    public List<ObjJExpr> getExprList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJExpr.class);
    }

    @NotNull
    public List<ObjJForStatement> getForStatementList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJForStatement.class);
    }

    @Override
    @NotNull
    public List<ObjJFunctionDeclaration> getFunctionDeclarationList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJFunctionDeclaration.class);
    }

    @Override
    @NotNull
    public List<ObjJIfStatement> getIfStatementList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJIfStatement.class);
    }

    @Override
    @NotNull
    public List<ObjJIncludeFile> getIncludeFileList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJIncludeFile.class);
    }

    @Override
    @NotNull
    public List<ObjJIncludeFramework> getIncludeFrameworkList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJIncludeFramework.class);
    }

    @Override
    @NotNull
    public List<ObjJPreprocessorIfStatement> getPreprocessorIfStatementList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJPreprocessorIfStatement.class);
    }

    @Override
    @NotNull
    public List<ObjJReturnStatement> getReturnStatementList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJReturnStatement.class);
    }

    @Override
    @NotNull
    public List<ObjJSwitchStatement> getSwitchStatementList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJSwitchStatement.class);
    }

    @Override
    @NotNull
    public List<ObjJThrowStatement> getThrowStatementList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJThrowStatement.class);
    }

    @Override
    @NotNull
    public List<ObjJTryStatement> getTryStatementList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJTryStatement.class);
    }

    @NotNull
    public List<ObjJWhileStatement> getWhileStatementList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJWhileStatement.class);
    }

    @Override
    @Nullable
    public PsiElement getOpenBrace() {
        return findChildByType(ObjJ_OPEN_BRACE);
    }

    @Nullable
    public PsiElement getCloseBrace() {
        return findChildByType(ObjJ_CLOSE_BRACE);
    }

    @NotNull
    public List<ObjJBlock> getBlockList() {
        return ObjJPsiImplUtil.getBlockList(this);
    }

    @Override
    public ObjJBlock getBlock() {
        return this;
    }

    @NotNull
    public List<ObjJBlockElement> getBlockElementList() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJBlockElement.class);
    }

    @NotNull
    public List<ObjJIterationStatement> getIterationStatementList() {
        return ObjJPsiImplUtil.getIterationStatementList(this);
    }

}
