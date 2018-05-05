package org.cappuccino_project.ide.intellij.plugin.psi.interfaces;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface ObjJCompositeElement extends PsiElement {
    Logger LOGGER = Logger.getLogger(ObjJCompositeElement.class.getName());

    @Nullable
    default ObjJFile getContainingObjJFile() {
        PsiFile file = getContainingFile();
        if (file == null) {
            LOGGER.log(Level.SEVERE, "Cannot get ObjJFile, as containing file is null.");
            return null;
        }
        if (file instanceof ObjJFile) {
            return (ObjJFile) file;
        }
        LOGGER.log(Level.SEVERE, "ObjJFile is actually of type: "+this.getContainingFile().getClass().getSimpleName());
        return null;
    }

    default <PsiT extends PsiElement> boolean isIn(Class<PsiT> parentClass) {
        return ObjJPsiImplUtil.isIn(this, parentClass);
    }

    @Nullable
    <PsiT extends PsiElement> PsiT getChildOfType(Class<PsiT> childClass);

    @NotNull
    <PsiT extends PsiElement> List<PsiT> getChildrenOfType(Class<PsiT> childClass);
    @Nullable
    <PsiT extends PsiElement> PsiT getParentOfType(Class<PsiT> childClass);

    @NotNull
    IElementType getElementType();

    //ObjJCompositeElement getPsiOrParent();
}
