package org.cappuccino_project.ide.intellij.plugin.contributor.handlers;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.parameterInfo.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjJMethodCallInfoHandler implements ParameterInfoHandler {
    @Override
    public boolean couldShowInLookup() {
        return true;
    }

    @Nullable
    @Override
    public Object[] getParametersForLookup(LookupElement lookupElement, ParameterInfoContext parameterInfoContext) {
        return new Object[0];
    }

    @Nullable
    @Override
    public Object findElementForParameterInfo(
            @NotNull
                    CreateParameterInfoContext createParameterInfoContext) {
        return null;
    }

    @Override
    public void showParameterInfo(
            @NotNull
                    Object o,
            @NotNull
                    CreateParameterInfoContext createParameterInfoContext) {

    }

    @Nullable
    @Override
    public Object findElementForUpdatingParameterInfo(
            @NotNull
                    UpdateParameterInfoContext updateParameterInfoContext) {
        return null;
    }

    @Override
    public void updateParameterInfo(
            @NotNull
                    Object o,
            @NotNull
                    UpdateParameterInfoContext updateParameterInfoContext) {

    }

    @Override
    public void updateUI(Object o,
                         @NotNull
                                 ParameterInfoUIContext parameterInfoUIContext) {

    }

    @Nullable
    @Override
    public Object[] getParametersForDocumentation(Object o, ParameterInfoContext parameterInfoContext) {
        return new Object[0];
    }

    @Nullable
    @Override
    public String getParameterCloseChars() {
        return null;
    }

    @Override
    public boolean tracksParameterIndex() {
        return false;
    }
}
