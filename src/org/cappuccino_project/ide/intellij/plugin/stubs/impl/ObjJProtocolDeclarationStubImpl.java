package org.cappuccino_project.ide.intellij.plugin.stubs.impl;

import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJProtocolDeclarationImpl;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJProtocolDeclarationStub;
import org.cappuccino_project.ide.intellij.plugin.stubs.types.ObjJStubTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ObjJProtocolDeclarationStubImpl extends ObjJClassDeclarationStubImpl<ObjJProtocolDeclarationImpl> implements ObjJProtocolDeclarationStub{
    public ObjJProtocolDeclarationStubImpl(
            @NotNull
                    StubElement parent, @NotNull
                    final String className, final List<String> protocols, final boolean shouldResolve) {
        super(parent, ObjJStubTypes.PROTOCOL, className, protocols, shouldResolve);
    }
}
