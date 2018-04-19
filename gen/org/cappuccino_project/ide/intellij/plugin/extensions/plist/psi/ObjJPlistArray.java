// This is a generated file. Not intended for manual editing.
package org.cappuccino_project.ide.intellij.plugin.extensions.plist.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ObjJPlistArray extends PsiElement {

  @NotNull
  List<ObjJPlistArray> getArrayList();

  @NotNull
  List<ObjJPlistBoolean> getBooleanList();

  @NotNull
  List<ObjJPlistDataValue> getDataValueList();

  @NotNull
  List<ObjJPlistDict> getDictList();

  @NotNull
  List<ObjJPlistInteger> getIntegerList();

  @NotNull
  List<ObjJPlistRealNumber> getRealNumberList();

  @NotNull
  List<ObjJPlistString> getStringList();

  @Nullable
  PsiElement getArrayClose();

  @NotNull
  PsiElement getArrayOpen();

}
