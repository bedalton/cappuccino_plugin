// This is a generated file. Not intended for manual editing.
package org.cappuccino_project.ide.intellij.plugin.extensions.plist.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ObjJPlistXmlHeader extends PsiElement {

  @NotNull
  List<ObjJPlistXmlTagProperty> getXmlTagPropertyList();

  @Nullable
  PsiElement getClosingHeaderBracket();

  @NotNull
  PsiElement getOpeningHeaderBracket();

  @Nullable
  PsiElement getXml();

}
