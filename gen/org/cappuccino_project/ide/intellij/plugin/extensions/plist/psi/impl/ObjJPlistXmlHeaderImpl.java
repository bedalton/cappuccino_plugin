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

public class ObjJPlistXmlHeaderImpl extends ASTWrapperPsiElement implements ObjJPlistXmlHeader {

  public ObjJPlistXmlHeaderImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ObjJPlistVisitor visitor) {
    visitor.visitXmlHeader(this);
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
  @Nullable
  public PsiElement getClosingHeaderBracket() {
    return findChildByType(ObjJPlist_CLOSING_HEADER_BRACKET);
  }

  @Override
  @NotNull
  public PsiElement getOpeningHeaderBracket() {
    return findNotNullChildByType(ObjJPlist_OPENING_HEADER_BRACKET);
  }

  @Override
  @Nullable
  public PsiElement getXml() {
    return findChildByType(ObjJPlist_XML);
  }

}
