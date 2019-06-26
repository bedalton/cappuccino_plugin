package cappuccino.ide.intellij.plugin.psi.impl;

import cappuccino.ide.intellij.plugin.psi.*;
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJBlock;
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJChildrenRequireSemiColons;
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJHasIgnoreStatements;
import cappuccino.ide.intellij.plugin.psi.interfaces.ObjJIterationStatement;
import cappuccino.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.ObjJ_OPEN_BRACE;

public class ObjJSingleBodyStatementMixin extends ObjJCompositeElementImpl  implements ObjJChildrenRequireSemiColons, ObjJHasIgnoreStatements, ObjJBlock {

    public ObjJSingleBodyStatementMixin(
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

    @Override
    @NotNull
    public PsiElement getOpenBrace() {
        return notNullChild(findChildByType(ObjJ_OPEN_BRACE));
    }

    @Override
    @NotNull
    public List<ObjJBlock> getBlockList() {
        return ObjJPsiImplUtil.getBlockList(this);
    }

    @Override
    @NotNull
    public List<ObjJIterationStatement> getIterationStatementList() {
        return ObjJPsiImplUtil.getIterationStatementList(this);
    }
}
