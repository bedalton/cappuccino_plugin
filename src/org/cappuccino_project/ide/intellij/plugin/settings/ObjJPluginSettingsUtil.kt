package org.cappuccino_project.ide.intellij.plugin.settings

import com.intellij.ide.util.PropertiesComponent

object ObjJPluginSettingsUtil {
    private val PREFIX = "org.cappuccino_project.ide.intellij.plugin.setting."

    /**
     * Set value or unset if equals to default value
     */
    private fun setValue(key: String, `val`: String, defaultValue: String) {
        PropertiesComponent.getInstance().setValue(PREFIX + key, `val`, defaultValue)
    }

    /**
     * Set value or unset if equals to default value
     */
    private fun setValue(key: String, `val`: Boolean, defaultValue: Boolean) {
        PropertiesComponent.getInstance().setValue(PREFIX + key, `val`, defaultValue)
    }

    /**
     * Set value or unset if equals to default value
     */
    private fun setValue(key: String, val1: Float, defaultValue: Float) {
        PropertiesComponent.getInstance().setValue(PREFIX + key, val1, defaultValue)
    }

    /**
     * Set value or unset if equals to default value
     */
    private fun setValue(key: String, val1: Int, defaultValue: Int) {
        PropertiesComponent.getInstance().setValue(PREFIX + key, val1, defaultValue)
    }

    /**
     * Set value or unset if equals to default value
     */
    private fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return PropertiesComponent.getInstance().getBoolean(PREFIX + key, defaultValue)
    }


    /**
     * Get value if set, or get default value
     */
    private fun getInt(key: String, defaultValue: Int): Int {
        return PropertiesComponent.getInstance().getInt(PREFIX + key, defaultValue)
    }

    /**
     * Get value if set, or get default value
     */
    fun getFloat(key: String, defaultValue: Float): Float {
        return PropertiesComponent.getInstance().getFloat(PREFIX + key, defaultValue)
    }

    /**
     * Get value if set, or get default value
     */
    private fun getValue(key: String, defaultValue: String): String {
        return PropertiesComponent.getInstance().getValue(PREFIX + key, defaultValue)
    }

    class StringSetting internal constructor(private val key: String, private val defaultValue: String) : Setting<String> {

        override var value: String?
            get() = ObjJPluginSettingsUtil.getValue(key, defaultValue)
            set(value) {
                var value = value
                if (value == null) {
                    value = defaultValue
                }
                ObjJPluginSettingsUtil.setValue(key, value, defaultValue)
            }
    }

    class IntegerSetting internal constructor(private val key: String, private val defaultValue: Int) : Setting<Int> {

        override var value: Int?
            get() = ObjJPluginSettingsUtil.getInt(key, defaultValue)
            set(value) {
                var value = value
                if (value == null) {
                    value = defaultValue
                }
                ObjJPluginSettingsUtil.setValue(key, value, defaultValue)
            }
    }

    class BooleanSetting internal constructor(private val key: String, private val defaultValue: Boolean) : Setting<Boolean> {

        override var value: Boolean?
            get() = ObjJPluginSettingsUtil.getBoolean(key, defaultValue)
            set(value) {
                var value = value
                if (value == null) {
                    value = defaultValue
                }
                ObjJPluginSettingsUtil.setValue(key, value, defaultValue)
            }
    }

    class FloatSetting internal constructor(private val key: String, private val defaultValue: Float) : Setting<Float> {

        override var value: Float?
            get() = ObjJPluginSettingsUtil.getFloat(key, defaultValue)
            set(value) {
                var value = value
                if (value == null) {
                    value = defaultValue
                }
                ObjJPluginSettingsUtil.setValue(key, value, defaultValue)
            }
    }

    class AnnotationLevelSetting internal constructor(private val key: String, private val defaultValue: AnnotationLevel) : Setting<AnnotationLevel> {

        override var value: AnnotationLevel?
            get() {
                val rawValue = ObjJPluginSettingsUtil.getInt(key, defaultValue.value)
                return AnnotationLevel.getAnnotationLevel(rawValue)
            }
            set(value) {
                var value = value
                if (value == null) {
                    value = defaultValue
                }
                ObjJPluginSettingsUtil.setValue(key, value.value, defaultValue.value)
            }
    }


    enum class AnnotationLevel private constructor(private val value: Int) {
        ERROR(100),
        WARNING(75),
        WEAK_WARNING(25),
        IGNORE(0);

        private fun getAnnotationLevel(value: Int): AnnotationLevel {
            return if (value >= ERROR.value) {
                ERROR
            } else if (value >= WARNING.value) {
                WARNING
            } else if (value >= WEAK_WARNING.value) {
                WEAK_WARNING
            } else {
                IGNORE
            }
        }
    }


    private interface Setting<T> {
        var value: T
    }


}
