package org.cappuccino_project.ide.intellij.plugin.lang;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJFilePsiUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class ObjJFile extends PsiFileBase implements ObjJCompositeElement {


    public ObjJFile(@NotNull
                                         FileViewProvider viewProvider) {
        super(viewProvider, ObjJLanguage.INSTANCE);
    }

    @Override
    public String toString() {
        return "ObjectiveJ Language file";
    }

    @Override
    public Icon getIcon(int flags) {
        return ObjJIcons.DOCUMENT_ICON;
    }

    @NotNull
    public List<ObjJClassDeclarationElement> getClassDeclarations() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJClassDeclarationElement.class);
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return ObjJFileType.INSTANCE;
    }

    @NotNull
    public List<String> getImportStrings() {
        return ObjJFilePsiUtil.getImportsAsStrings(this);
    }

    @Override
    @Nullable
    public <PsiT extends PsiElement> PsiT getChildOfType(Class<PsiT> childClass) {
        return ObjJTreeUtil.getChildOfType(this, childClass);
    }

    @Override
    @NotNull
    public <PsiT extends PsiElement> List<PsiT> getChildrenOfType(Class<PsiT> childClass) {
        return ObjJTreeUtil.getChildrenOfTypeAsList(this, childClass);
    }

    @Override
    @Nullable
    public <PsiT extends PsiElement> PsiT getParentOfType(Class<PsiT> parentClass) {
        return ObjJTreeUtil.getParentOfType(this, parentClass);
    }

    @NotNull
    @Override
    public IElementType getElementType() {
        return ObjJStubTypes.FILE;
    }

}
