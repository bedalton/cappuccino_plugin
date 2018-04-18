package org.cappuccino_project.ide.intellij.plugin.stubs.impl;

import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;

public class ObjJStubBaseImpl<PsiT extends StubBasedPsiElement<? extends StubElement>> extends StubBase<PsiT> {
    protected ObjJStubBaseImpl(StubElement parent, IStubElementType elementType) {
        super(parent, elementType);
    }
}
