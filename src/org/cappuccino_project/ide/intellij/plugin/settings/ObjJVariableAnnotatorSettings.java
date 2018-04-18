package org.cappuccino_project.ide.intellij.plugin.settings;

import org.cappuccino_project.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.*;

public class ObjJVariableAnnotatorSettings {

    //VariableAnnotatorSettings
    private static final String OVERSHADOWS_FILE_VARIABLE_ANNOTATION_LEVEL = "annoator.variable.overshadows.file_variable";
    private static final AnnotationLevel OVERSHADOWS_FILE_VARIABLE_ANNOTATION_LEVEL_DEFAULT = AnnotationLevel.WEAK_WARNING;
    public static final AnnotationLevelSetting OVERSHADOWS_FILE_VARIABLE_SETTING = new AnnotationLevelSetting(OVERSHADOWS_FILE_VARIABLE_ANNOTATION_LEVEL, OVERSHADOWS_FILE_VARIABLE_ANNOTATION_LEVEL_DEFAULT);

    private static final String OVERSHADOWS_INSTANCE_VARIABLE_ANNOTATION_LEVEL = "annoator.variable.overshadows.instance_variable";
    private static final AnnotationLevel OVERSHADOWS_INSTANCE_VARIABLE_ANNOTATION_LEVEL_DEFAULT = AnnotationLevel.WARNING;
    public static final AnnotationLevelSetting OVERSHADOWS_INSTANCE_VARIABLE_SETTING = new AnnotationLevelSetting(OVERSHADOWS_INSTANCE_VARIABLE_ANNOTATION_LEVEL, OVERSHADOWS_INSTANCE_VARIABLE_ANNOTATION_LEVEL_DEFAULT);

    private static final String OVERSHADOWS_METHOD_VARIABLE_ANNOTATION_LEVEL = "annoator.variable.overshadows.method_variable";
    private static final AnnotationLevel OVERSHADOWS_METHOD_VARIABLE_ANNOTATION_LEVEL_DEFAULT = AnnotationLevel.WARNING;
    public static final AnnotationLevelSetting OVERSHADOWS_METHOD_VARIABLE_SETTING = new AnnotationLevelSetting(OVERSHADOWS_METHOD_VARIABLE_ANNOTATION_LEVEL, OVERSHADOWS_METHOD_VARIABLE_ANNOTATION_LEVEL_DEFAULT);

    private static final String OVERSHADOWS_BLOCK_VARIABLE_ANNOTATION_LEVEL = "annoator.variable.overshadows.block_variable";
    private static final AnnotationLevel OVERSHADOWS_BLOCK_VARIABLE_ANNOTATION_LEVEL_DEFAULT = AnnotationLevel.WARNING;
    public static final AnnotationLevelSetting OVERSHADOWS_BLOCK_VARIABLE_SETTING = new AnnotationLevelSetting(OVERSHADOWS_BLOCK_VARIABLE_ANNOTATION_LEVEL, OVERSHADOWS_BLOCK_VARIABLE_ANNOTATION_LEVEL_DEFAULT);
}
