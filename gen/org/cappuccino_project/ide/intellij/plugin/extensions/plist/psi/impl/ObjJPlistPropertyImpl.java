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

public class ObjJPlistPropertyImpl extends ASTWrapperPsiElement implements ObjJPlistProperty {

  public ObjJPlistPropertyImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ObjJPlistVisitor visitor) {
    visitor.visitProperty(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ObjJPlistVisitor) accept((ObjJPlistVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ObjJPlistArray> getArrayList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJPlistArray.class);
  }

  @Override
  @NotNull
  public List<ObjJPlistBoolean> getBooleanList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJPlistBoolean.class);
  }

  @Override
  @NotNull
  public List<ObjJPlistDataValue> getDataValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJPlistDataValue.class);
  }

  @Override
  @NotNull
  public List<ObjJPlistDict> getDictList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJPlistDict.class);
  }

  @Override
  @NotNull
  public List<ObjJPlistInteger> getIntegerList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJPlistInteger.class);
  }

  @Override
  @NotNull
  public ObjJPlistKeyProperty getKeyProperty() {
    return findNotNullChildByClass(ObjJPlistKeyProperty.class);
  }

  @Override
  @NotNull
  public List<ObjJPlistRealNumber> getRealNumberList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJPlistRealNumber.class);
  }

  @Override
  @NotNull
  public List<ObjJPlistString> getStringList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ObjJPlistString.class);
  }

}
