package org.cappuccino_project.ide.intellij.plugin.lang;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJFrameworkReference;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJImportFramework;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJImportStatement;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJFilePsiUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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


}
