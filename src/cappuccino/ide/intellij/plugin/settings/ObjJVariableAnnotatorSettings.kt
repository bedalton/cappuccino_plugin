package cappuccino.ide.intellij.plugin.settings

import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.AnnotationLevel.WARNING
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.AnnotationLevel.WEAK_WARNING
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.AnnotationLevelSetting

object ObjJVariableAnnotatorSettings {

    //VariableAnnotatorSettings
    private const val OVERSHADOWS_FILE_VARIABLE_ANNOTATION_LEVEL = "annoator.variable.overshadows.file_variable"
    private val OVERSHADOWS_FILE_VARIABLE_ANNOTATION_LEVEL_DEFAULT = WEAK_WARNING
    val OVERSHADOWS_FILE_VARIABLE_SETTING = AnnotationLevelSetting(OVERSHADOWS_FILE_VARIABLE_ANNOTATION_LEVEL, OVERSHADOWS_FILE_VARIABLE_ANNOTATION_LEVEL_DEFAULT)

    private const val OVERSHADOWS_INSTANCE_VARIABLE_ANNOTATION_LEVEL = "annoator.variable.overshadows.instance_variable"
    private val OVERSHADOWS_INSTANCE_VARIABLE_ANNOTATION_LEVEL_DEFAULT = WARNING
    val OVERSHADOWS_INSTANCE_VARIABLE_SETTING = AnnotationLevelSetting(OVERSHADOWS_INSTANCE_VARIABLE_ANNOTATION_LEVEL, OVERSHADOWS_INSTANCE_VARIABLE_ANNOTATION_LEVEL_DEFAULT)

    private const val OVERSHADOWS_METHOD_VARIABLE_ANNOTATION_LEVEL = "annoator.variable.overshadows.method_variable"
    private val OVERSHADOWS_METHOD_VARIABLE_ANNOTATION_LEVEL_DEFAULT = WARNING
    val OVERSHADOWS_METHOD_VARIABLE_SETTING = AnnotationLevelSetting(OVERSHADOWS_METHOD_VARIABLE_ANNOTATION_LEVEL, OVERSHADOWS_METHOD_VARIABLE_ANNOTATION_LEVEL_DEFAULT)

    private const val OVERSHADOWS_BLOCK_VARIABLE_ANNOTATION_LEVEL = "annoator.variable.overshadows.block_variable"
    private val OVERSHADOWS_BLOCK_VARIABLE_ANNOTATION_LEVEL_DEFAULT = WEAK_WARNING
    val OVERSHADOWS_BLOCK_VARIABLE_SETTING = AnnotationLevelSetting(OVERSHADOWS_BLOCK_VARIABLE_ANNOTATION_LEVEL, OVERSHADOWS_BLOCK_VARIABLE_ANNOTATION_LEVEL_DEFAULT)
}
