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

public class ObjJPlistArrayImpl extends ASTWrapperPsiElement implements ObjJPlistArray {

  public ObjJPlistArrayImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ObjJPlistVisitor visitor) {
    visitor.visitArray(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ObjJPlistVisitor) accept((ObjJPlistVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ObjJPlistArray getArray() {
    return findChildByClass(ObjJPlistArray.class);
  }

  @Override
  @Nullable
  public ObjJPlistBoolean getBoolean() {
    return findChildByClass(ObjJPlistBoolean.class);
  }

  @Override
  @Nullable
  public ObjJPlistDataValue getDataValue() {
    return findChildByClass(ObjJPlistDataValue.class);
  }

  @Override
  @Nullable
  public ObjJPlistDict getDict() {
    return findChildByClass(ObjJPlistDict.class);
  }

  @Override
  @Nullable
  public ObjJPlistInteger getInteger() {
    return findChildByClass(ObjJPlistInteger.class);
  }

  @Override
  @Nullable
  public ObjJPlistRealNumber getRealNumber() {
    return findChildByClass(ObjJPlistRealNumber.class);
  }

  @Override
  @Nullable
  public ObjJPlistString getString() {
    return findChildByClass(ObjJPlistString.class);
  }

  @Override
  @Nullable
  public PsiElement getArrayClose() {
    return findChildByType(ObjJPlist_ARRAY_CLOSE);
  }

  @Override
  @NotNull
  public PsiElement getArrayOpen() {
    return findNotNullChildByType(ObjJPlist_ARRAY_OPEN);
  }

}
