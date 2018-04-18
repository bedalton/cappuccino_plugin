/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cappuccino_project.ide.intellij.plugin.psi.impl;

import com.intellij.extapi.psi.StubBasedPsiElementBase;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.IncorrectOperationException;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJLanguage;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJElementUtils;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJStubBasedElement;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubElementType;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class ObjJElementImplStub<T extends StubElement<? extends PsiElement>> extends StubBasedPsiElementBase<T>
        implements ObjJCompositeElement, ObjJStubBasedElement<T> {
    public ObjJElementImplStub(@NotNull T stub, @NotNull IStubElementType nodeType) {
        super(stub, nodeType);
    }

    public ObjJElementImplStub(@NotNull ASTNode node) {
        super(node);
    }

    @NotNull
    @Override
    public Language getLanguage() {
        return ObjJLanguage.INSTANCE;
    }

    @Override
    public String toString() {
        return getElementType().toString();
    }

    @NotNull
    @Override
    public ObjJFile getContainingObjJFile() {
        PsiFile file = getContainingFile();
        assert file instanceof ObjJFile : "KtElement not inside KtFile: " + file + " " + (file.isValid() ? file.getText() : "<invalid>");
        return (ObjJFile) file;
    }

    @Override
    public void delete() throws IncorrectOperationException {
        ObjJElementUtils.deleteSemicolon(this);
        super.delete();
    }

    @NotNull
    protected <PsiT extends ObjJStubBasedElement<?>, StubT extends StubElement> List<PsiT> getStubOrPsiChildrenAsList(
            @NotNull
                    ObjJStubElementType<StubT, PsiT> elementType
    ) {
        return Arrays.asList(getStubOrPsiChildren(elementType, elementType.getArrayFactory()));
    }

    @NotNull
    public ObjJCompositeElement getPsiOrParent() {
        return this;
    }

    @Override
    @NotNull
    public <PsiT extends PsiElement> List<PsiT> getChildrenOfType(Class<PsiT> childClass) {
        return ObjJTreeUtil.getChildrenOfTypeAsList(this, childClass);
    }

    @Override
    @Nullable
    public <PsiT extends PsiElement> PsiT getChildOfType(Class<PsiT> parentClass) {
        return ObjJTreeUtil.getChildOfType(this, parentClass);
    }

    @Override
    @Nullable
    public <PsiT extends PsiElement> PsiT getParentOfType(Class<PsiT> parentClass) {
        return ObjJTreeUtil.getParentOfType(this, parentClass);
    }
}