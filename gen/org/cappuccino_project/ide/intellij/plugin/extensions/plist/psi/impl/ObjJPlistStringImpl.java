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

public class ObjJPlistStringImpl extends ASTWrapperPsiElement implements ObjJPlistString {

  public ObjJPlistStringImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ObjJPlistVisitor visitor) {
    visitor.visitString(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ObjJPlistVisitor) accept((ObjJPlistVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ObjJPlistStringValue getStringValue() {
    return findChildByClass(ObjJPlistStringValue.class);
  }

  @Override
  @Nullable
  public PsiElement getStringClose() {
    return findChildByType(ObjJPlist_STRING_CLOSE);
  }

  @Override
  @NotNull
  public PsiElement getStringOpen() {
    return findNotNullChildByType(ObjJPlist_STRING_OPEN);
  }

}
