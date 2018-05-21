package cappuccino.ide.intellij.plugin.contributor.handlers;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.lang.parameterInfo.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjJParameterInfo<ParameterOwner, ParameterType> implements ParameterInfoHandler<ParameterOwner, ParameterType> {
    @Override
    public boolean couldShowInLookup() {
        return false;
    }

    @Nullable
    @Override
    public Object[] getParametersForLookup(LookupElement lookupElement, ParameterInfoContext parameterInfoContext) {
        return new Object[0];
    }

    @Nullable
    @Override
    public ParameterOwner findElementForParameterInfo(
            @NotNull
                    CreateParameterInfoContext createParameterInfoContext) {
        return null;
    }

    @Override
    public void showParameterInfo(
            @NotNull
                    ParameterOwner parameterOwner,
            @NotNull
                    CreateParameterInfoContext createParameterInfoContext) {

    }

    @Nullable
    @Override
    public ParameterOwner findElementForUpdatingParameterInfo(
            @NotNull
                    UpdateParameterInfoContext updateParameterInfoContext) {
        return null;
    }

    @Override
    public void updateParameterInfo(
            @NotNull
                    ParameterOwner parameterOwner,
            @NotNull
                    UpdateParameterInfoContext updateParameterInfoContext) {

    }

    @Override
    public void updateUI(ParameterType parameterType,
                         @NotNull
                                 ParameterInfoUIContext parameterInfoUIContext) {

    }
}
