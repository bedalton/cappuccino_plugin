package org.cappuccino_project.ide.intellij.plugin.psi.interfaces;

import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.StubElement;

public interface ObjJStubBasedElement<StubT extends StubElement> extends  StubBasedPsiElement<StubT> {
}
