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

public class ObjJPlistPlistOpenTagImpl extends ASTWrapperPsiElement implements ObjJPlistPlistOpenTag {

  public ObjJPlistPlistOpenTagImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ObjJPlistVisitor visitor) {
    visitor.visitPlistOpenTag(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ObjJPlistVisitor) accept((ObjJPlistVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ObjJPlistXmlTagProperty> getXmlTagPropertyList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJPlistXmlTagProperty.class);
  }

  @Override
  @NotNull
  public PsiElement getGt() {
    return findNotNullChildByType(ObjJPlist_GT);
  }

  @Override
  @NotNull
  public PsiElement getPlistOpen() {
    return findNotNullChildByType(ObjJPlist_PLIST_OPEN);
  }

}
