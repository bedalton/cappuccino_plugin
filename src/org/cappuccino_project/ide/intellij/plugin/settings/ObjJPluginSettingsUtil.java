package org.cappuccino_project.ide.intellij.plugin.settings;

import com.intellij.ide.util.PropertiesComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjJPluginSettingsUtil {
    private static final String PREFIX = "org.cappuccino_project.ide.intellij.plugin.setting.";

    /**
     * Set value or unset if equals to default value
     */
    private static void setValue(@NotNull String key, String val, String defaultValue) {
        PropertiesComponent.getInstance().setValue(PREFIX+key, val, defaultValue);
    }

    /**
     * Set value or unset if equals to default value
     */
    private static void setValue(@NotNull String key, boolean val, boolean defaultValue) {
        PropertiesComponent.getInstance().setValue(PREFIX+key, val, defaultValue);
    }

    /**
     * Set value or unset if equals to default value
     */
    private static void setValue(@NotNull String key, float val1, float defaultValue) {
        PropertiesComponent.getInstance().setValue(PREFIX+key, val1, defaultValue);
    }

    /**
     * Set value or unset if equals to default value
     */
    private static void setValue(@NotNull String key, int val1, int defaultValue) {
        PropertiesComponent.getInstance().setValue(PREFIX+key, val1, defaultValue);
    }

    /**
     * Set value or unset if equals to default value
     */
    private static boolean getBoolean(@NotNull String key, boolean defaultValue) {
        return PropertiesComponent.getInstance().getBoolean(PREFIX + key, defaultValue);
    }


    /**
     * Get value if set, or get default value
     */
    private static int getInt(@NotNull String key, int defaultValue) {
        return PropertiesComponent.getInstance().getInt(PREFIX + key, defaultValue);
    }

    /**
     * Get value if set, or get default value
     */
    public static float getFloat(@NotNull String key, float defaultValue) {
        return PropertiesComponent.getInstance().getFloat(PREFIX + key, defaultValue);
    }

    /**
     * Get value if set, or get default value
     */
    private static String getValue(@NotNull String key, String defaultValue) {
        return PropertiesComponent.getInstance().getValue(PREFIX + key, defaultValue);
    }

    public static class StringSetting implements Setting<String>{
        private final String key;
        private final String defaultValue;

        StringSetting(@NotNull String key, @NotNull String defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @NotNull
        public String getValue() {
            return ObjJPluginSettingsUtil.getValue(key, defaultValue);
        }

        public void setValue(@Nullable String value) {
            if (value == null) {
                value = defaultValue;
            }
            ObjJPluginSettingsUtil.setValue(key, value, defaultValue);
        }
    }

    public static class IntegerSetting implements Setting<Integer> {
        private final String key;
        private final int defaultValue;

        IntegerSetting(@NotNull String key, int defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @NotNull
        public Integer getValue() {
            return ObjJPluginSettingsUtil.getInt(key, defaultValue);
        }

        public void setValue(@Nullable Integer value) {
            if (value == null) {
                value = defaultValue;
            }
            ObjJPluginSettingsUtil.setValue(key, value, defaultValue);
        }
    }

    public static class BooleanSetting implements Setting<Boolean>{
        private final String key;
        private final boolean defaultValue;

        BooleanSetting(@NotNull String key, boolean defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @NotNull
        public Boolean getValue() {
            return ObjJPluginSettingsUtil.getBoolean(key, defaultValue);
        }

        public void setValue(@Nullable Boolean value) {
            if (value == null) {
                value = defaultValue;
            }
            ObjJPluginSettingsUtil.setValue(key, value, defaultValue);
        }
    }

    public static class FloatSetting implements Setting<Float>{
        private final String key;
        private final float defaultValue;

        FloatSetting(@NotNull String key, float defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        @NotNull
        public Float getValue() {
            return ObjJPluginSettingsUtil.getFloat(key, defaultValue);
        }

        public void setValue(@Nullable Float value) {
            if (value == null) {
                value = defaultValue;
            }
            ObjJPluginSettingsUtil.setValue(key, value, defaultValue);
        }
    }

    public static class AnnotationLevelSetting implements Setting<AnnotationLevel> {
        private final String key;
        private final AnnotationLevel defaultValue;

        AnnotationLevelSetting(@NotNull String key, AnnotationLevel defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        public AnnotationLevel getValue() {
            final int rawValue = ObjJPluginSettingsUtil.getInt(key, defaultValue.value);
            return AnnotationLevel.getAnnotationLevel(rawValue);
        }

        public void setValue(@Nullable AnnotationLevel value) {
            if (value == null) {
                value = defaultValue;
            }
            ObjJPluginSettingsUtil.setValue(key, value.value, defaultValue.value);
        }
    }


    public enum AnnotationLevel {
        ERROR(100),
        WARNING(75),
        WEAK_WARNING(25),
        IGNORE(0);
        private final int value;
        AnnotationLevel(final int value) {
            this.value = value;
        }

        private static AnnotationLevel getAnnotationLevel(final int value) {
            if (value >= ERROR.value) {
                return ERROR;
            } else if (value >= WARNING.value) {
                return WARNING;
            } else if (value >= WEAK_WARNING.value) {
                return WEAK_WARNING;
            } else {
                return IGNORE;
            }
        }
    }


    private interface Setting<T> {
        T getValue();
        void setValue(T defaultValue);
    }


}
