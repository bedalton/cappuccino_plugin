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

public class ObjJPlistDocTypeImpl extends ASTWrapperPsiElement implements ObjJPlistDocType {

  public ObjJPlistDocTypeImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ObjJPlistVisitor visitor) {
    visitor.visitDocType(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ObjJPlistVisitor) accept((ObjJPlistVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ObjJPlistDocTypeParamList getDocTypeParamList() {
    return findChildByClass(ObjJPlistDocTypeParamList.class);
  }

  @Override
  @NotNull
  public PsiElement getDoctypeOpen() {
    return findNotNullChildByType(ObjJPlist_DOCTYPE_OPEN);
  }

  @Override
  @Nullable
  public PsiElement getGt() {
    return findChildByType(ObjJPlist_GT);
  }

}
