// This is a generated file. Not intended for manual editing.
package org.cappuccino_project.ide.intellij.plugin.extensions.plist.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ObjJPlistArray extends PsiElement {

  @Nullable
  ObjJPlistArray getArray();

  @Nullable
  ObjJPlistBoolean getBoolean();

  @Nullable
  ObjJPlistDataValue getDataValue();

  @Nullable
  ObjJPlistDict getDict();

  @Nullable
  ObjJPlistInteger getInteger();

  @Nullable
  ObjJPlistRealNumber getRealNumber();

  @Nullable
  ObjJPlistString getString();

  @Nullable
  PsiElement getArrayClose();

  @NotNull
  PsiElement getArrayOpen();

}
