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

public class ObjJPlistXmlTagPropertyImpl extends ASTWrapperPsiElement implements ObjJPlistXmlTagProperty {

  public ObjJPlistXmlTagPropertyImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ObjJPlistVisitor visitor) {
    visitor.visitXmlTagProperty(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ObjJPlistVisitor) accept((ObjJPlistVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ObjJPlistStringLiteral getStringLiteral() {
    return findChildByClass(ObjJPlistStringLiteral.class);
  }

  @Override
  @NotNull
  public PsiElement getEquals() {
    return findNotNullChildByType(ObjJPlist_EQUALS);
  }

  @Override
  @Nullable
  public PsiElement getVersion() {
    return findChildByType(ObjJPlist_VERSION);
  }

  @Override
  @Nullable
  public PsiElement getXmlTagPropertyKey() {
    return findChildByType(ObjJPlist_XML_TAG_PROPERTY_KEY);
  }

}
