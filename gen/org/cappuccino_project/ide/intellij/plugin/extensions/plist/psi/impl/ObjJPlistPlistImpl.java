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

public class ObjJPlistPlistImpl extends ASTWrapperPsiElement implements ObjJPlistPlist {

  public ObjJPlistPlistImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ObjJPlistVisitor visitor) {
    visitor.visitPlist(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ObjJPlistVisitor) accept((ObjJPlistVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ObjJPlistDict getDict() {
    return findChildByClass(ObjJPlistDict.class);
  }

  @Override
  @NotNull
  public ObjJPlistPlistOpenTag getPlistOpenTag() {
    return findNotNullChildByClass(ObjJPlistPlistOpenTag.class);
  }

  @Override
  @Nullable
  public PsiElement getPlistClose() {
    return findChildByType(ObjJPlist_PLIST_CLOSE);
  }

}
