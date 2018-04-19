// This is a generated file. Not intended for manual editing.
package org.cappuccino_project.ide.intellij.plugin.extensions.plist.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.cappuccino_project.ide.intellij.plugin.extensions.plist.psi.types.ObjJPlistTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.cappuccino_project.ide.intellij.plugin.extensions.plist.psi.*;

public class ObjJPlistDictImpl extends ASTWrapperPsiElement implements ObjJPlistDict {

  public ObjJPlistDictImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ObjJPlistVisitor visitor) {
    visitor.visitDict(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ObjJPlistVisitor) accept((ObjJPlistVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ObjJPlistProperty> getPropertyList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJPlistProperty.class);
  }

  @Override
  @Nullable
  public PsiElement getDictClose() {
    return findChildByType(ObjJPlist_DICT_CLOSE);
  }

  @Override
  @NotNull
  public PsiElement getDictOpen() {
    return findNotNullChildByType(ObjJPlist_DICT_OPEN);
  }

}
