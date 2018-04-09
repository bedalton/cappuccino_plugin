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

public class ObjJPlistDataValueImpl extends ASTWrapperPsiElement implements ObjJPlistDataValue {

  public ObjJPlistDataValueImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ObjJPlistVisitor visitor) {
    visitor.visitDataValue(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ObjJPlistVisitor) accept((ObjJPlistVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getDataClose() {
    return findNotNullChildByType(ObjJPlist_DATA_CLOSE);
  }

  @Override
  @NotNull
  public PsiElement getDataLiteral() {
    return findNotNullChildByType(ObjJPlist_DATA_LITERAL);
  }

  @Override
  @NotNull
  public PsiElement getDataOpen() {
    return findNotNullChildByType(ObjJPlist_DATA_OPEN);
  }

}
