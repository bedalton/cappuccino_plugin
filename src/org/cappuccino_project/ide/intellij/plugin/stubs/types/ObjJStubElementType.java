package org.cappuccino_project.ide.intellij.plugin.stubs.types;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IStubFileElementType;
import com.intellij.util.ArrayFactory;
import com.intellij.util.ReflectionUtil;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJLanguage;
import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJClassDeclarationElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJStubBasedElement;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJPsiImplUtil;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class ObjJStubElementType<StubT extends StubElement, PsiT extends ObjJStubBasedElement<?>> extends IStubElementType<StubT, PsiT> {

    @NotNull
    private final Constructor<PsiT> byNodeConstructor;
    @NotNull
    private final PsiT[] emptyArray;
    @NotNull
    private final ArrayFactory<PsiT> arrayFactory;

    public ObjJStubElementType(@NotNull @NonNls String debugName, @NotNull Class<PsiT> psiClass, @NotNull Class<?> stubClass) {
        super(debugName, ObjJLanguage.INSTANCE);
        try {
            byNodeConstructor = psiClass.getConstructor(ASTNode.class);
        }
        catch (NoSuchMethodException e) {
            throw new RuntimeException("Stub element type declaration for " + psiClass.getSimpleName() + " is missing required constructors",e);
        }
        //noinspection unchecked
        emptyArray = (PsiT[]) Array.newInstance(psiClass, 0);
        arrayFactory = count -> {
            if (count == 0) {
                return emptyArray;
            }
            //noinspection unchecked
            return (PsiT[]) Array.newInstance(psiClass, count);
        };
    }

    @NotNull
    public PsiT createPsiFromAst(@NotNull ASTNode node) {
        return ReflectionUtil.createInstance(byNodeConstructor, node);
    }

    @NotNull
    @Override
    public String getExternalId() {
        return "objj." + toString();
    }

    @Override
    public boolean shouldCreateStub(ASTNode node) {
        return true;
    }

    @Override
    public void indexStub(@NotNull StubT stub, @NotNull IndexSink sink) {
        // do not force inheritors to implement this method
    }

    @NotNull
    public ArrayFactory<PsiT> getArrayFactory() {
        return arrayFactory;
    }

    protected boolean shouldResolve(ASTNode node) {
        PsiElement psiElement = node.getPsi();
        ObjJClassDeclarationElement classDeclarationElement = ObjJPsiImplUtil.getContainingClass(psiElement);
        if (classDeclarationElement == null) {
            return false;
        }
        return ObjJPsiImplUtil.shouldResolve(classDeclarationElement);
    }
}