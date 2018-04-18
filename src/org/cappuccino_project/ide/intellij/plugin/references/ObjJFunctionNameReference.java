package org.cappuccino_project.ide.intellij.plugin.references;

import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.DumbServiceImpl;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.cappuccino_project.ide.intellij.plugin.indices.ObjJFunctionsIndex;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ObjJFunctionNameReference extends PsiReferenceBase<ObjJFunctionName> {

    private static final Logger LOGGER = Logger.getLogger(ObjJFunctionNameReference.class.getName());
    private final String functionName;
    private final PsiFile file;

    public ObjJFunctionNameReference(ObjJFunctionName functionName) {
        super(functionName, TextRange.create(0, functionName.getTextLength()));
        this.functionName = functionName.getText();
        //LOGGER.log(Level.INFO, "Created function name resolver with text: <"+this.functionName+"> and canonText: <"+getCanonicalText()+">");
        file = functionName.getContainingFile();
    }

    @Override
    public boolean isReferenceTo(PsiElement element) {
        boolean isCorrectReference = (element instanceof ObjJVariableName || element instanceof ObjJFunctionName);
        if (ObjJTreeUtil.getParentOfType(element, ObjJFunctionCall.class) != null) {
            isCorrectReference = isCorrectReference && ObjJTreeUtil.getParentOfType(element, ObjJFunctionDeclarationElement.class) != null;
        }
        return isCorrectReference && element.getText().equals(functionName);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        if (DumbServiceImpl.isDumb(myElement.getProject())) {
            return null;
        }
        List<PsiElement> allOut = new ArrayList<>();
        //LOGGER.log(Level.INFO, "There are <"+ObjJFunctionsIndex.getInstance().getAllKeys(myElement.getProject()).size()+"> function in index");
        for (ObjJFunctionDeclarationElement functionDeclaration : ObjJFunctionsIndex.getInstance().get(functionName, myElement.getProject())) {
            ProgressIndicatorProvider.checkCanceled();
            allOut.add(functionDeclaration.getFunctionNameNode());
            if (functionDeclaration.getContainingFile().isEquivalentTo(file)) {
                return functionDeclaration.getFunctionNameNode();
            }
        }
        for (ObjJPreprocessorDefineFunction function : ObjJTreeUtil.getChildrenOfTypeAsList(myElement.getContainingFile(), ObjJPreprocessorDefineFunction.class)) {
            if (function.getFunctionName() != null && function.getFunctionName().getText().equals(myElement.getText())) {
                return function.getFunctionName();
            }
        }
        return !allOut.isEmpty() ?  allOut.get(0) : null;
    }

    @Override
    @NotNull
    public PsiElement handleElementRename(@NotNull String newFunctionName) {
        return ObjJPsiImplUtil.setName(myElement, newFunctionName);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

}
