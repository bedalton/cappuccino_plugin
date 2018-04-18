package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJFunctionLiteral;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJFunctionDeclarationImpl;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJFunctionDeclarationElement;

import java.util.List;

public interface ObjJFunctionDeclarationElementStub<PsiT extends ObjJFunctionDeclarationElement> extends StubElement<PsiT> {
    String getFileName();
    String getFqName();
    String getFunctionName();
    int getNumParams();
    List<String> getParamNames();
    String getReturnType();
}
