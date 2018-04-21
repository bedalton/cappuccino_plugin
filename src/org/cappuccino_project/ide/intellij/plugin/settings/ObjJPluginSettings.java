package org.cappuccino_project.ide.intellij.plugin.settings;

import com.intellij.ide.util.PropertiesComponent;
import org.jetbrains.annotations.NotNull;

import static org.cappuccino_project.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.*;

public class ObjJPluginSettings {

    //EOS
    private static final String INFER_EOS_KEY = "parser.INFER_EOS";
    private static final boolean INFER_EOS_DEFAULT = false;
    private static BooleanSetting inferEOS = new BooleanSetting(INFER_EOS_KEY, INFER_EOS_DEFAULT);

    //CallTarget
    private static final String VALIDATE_CALL_TARGET = "resolve.calltarget.RESOLVE_CALL_TARGET";
    private static final boolean VALIDATE_CALL_TARGET_DEFAULT = false;
    private static BooleanSetting validateCallTarget = new BooleanSetting(VALIDATE_CALL_TARGET, VALIDATE_CALL_TARGET_DEFAULT);

    public static boolean inferEOS() {
        return inferEOS.getValue();
    }

    public static void inferEos(boolean infer) {
        inferEOS.setValue(infer);
    }

    public static boolean validateCallTarget() {
        return validateCallTarget.getValue();
    }

    public static void validateCallTarget(final boolean newValidateCallTargetValue) {
        validateCallTarget.setValue(newValidateCallTargetValue);
    }



}
