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

public class ObjJPlistRealNumberImpl extends ASTWrapperPsiElement implements ObjJPlistRealNumber {

  public ObjJPlistRealNumberImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ObjJPlistVisitor visitor) {
    visitor.visitRealNumber(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ObjJPlistVisitor) accept((ObjJPlistVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getDecimalLiteral() {
    return findChildByType(ObjJPlist_DECIMAL_LITERAL);
  }

  @Override
  @Nullable
  public PsiElement getRealClose() {
    return findChildByType(ObjJPlist_REAL_CLOSE);
  }

  @Override
  @NotNull
  public PsiElement getRealOpen() {
    return findNotNullChildByType(ObjJPlist_REAL_OPEN);
  }

}
