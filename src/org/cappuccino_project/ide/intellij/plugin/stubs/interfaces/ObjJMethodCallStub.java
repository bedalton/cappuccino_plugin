package org.cappuccino_project.ide.intellij.plugin.stubs.interfaces;


import com.intellij.psi.stubs.StubElement;
import org.cappuccino_project.ide.intellij.plugin.psi.impl.ObjJMethodCallImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ObjJMethodCallStub extends StubElement<ObjJMethodCallImpl>, ObjJResolveableStub<ObjJMethodCallImpl> {

    @NotNull
    String getCallTarget();

    @NotNull
    List<String> getPossibleCallTargetTypes();

    @NotNull
    List<String> getSelectorStrings();

    @NotNull
    String getSelectorString();

    @Nullable
    String getContainingClassName();;

}
