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

public class ObjJPlistStringLiteralImpl extends ASTWrapperPsiElement implements ObjJPlistStringLiteral {

  public ObjJPlistStringLiteralImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ObjJPlistVisitor visitor) {
    visitor.visitStringLiteral(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ObjJPlistVisitor) accept((ObjJPlistVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getDoubleQuoteStringLiteral() {
    return findChildByType(ObjJPlist_DOUBLE_QUOTE_STRING_LITERAL);
  }

  @Override
  @Nullable
  public PsiElement getSingleQuoteStringLiteral() {
    return findChildByType(ObjJPlist_SINGLE_QUOTE_STRING_LITERAL);
  }

}
