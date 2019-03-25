@file:Suppress("unused")

package cappuccino.ide.intellij.plugin.settings

import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.BooleanSetting

object ObjJPluginSettings {

    //EOS
    private const val INFER_EOS_KEY = "objj.parser.INFER_EOS"
    private const val INFER_EOS_DEFAULT = false
    private val inferEOS = BooleanSetting(INFER_EOS_KEY, INFER_EOS_DEFAULT)

    //CallTarget
    private const val VALIDATE_CALL_TARGET = "objj.resolve.calltarget.VALIDATE_CALL_TARGET"
    private const val VALIDATE_CALL_TARGET_DEFAULT = false
    private val validateCallTarget = BooleanSetting(VALIDATE_CALL_TARGET, VALIDATE_CALL_TARGET_DEFAULT)

    // Resolve call target
    private const val RESOLVE_CALL_TARGET = "objj.resolve.calltarget.RESOLVE_CALL_TARGET"
    private const val RESOLVE_CALL_DEFAULT = true
    private val resolveCallTargetSetting = BooleanSetting(RESOLVE_CALL_TARGET, RESOLVE_CALL_DEFAULT)
    var resolveCallTargetFromAssignments:Boolean get() {
        return resolveCallTargetSetting.value!!
    } set (value) {
        resolveCallTargetSetting.value = value
    }

    private const val RESOLVE_CALL_TARGET_FILTER_STRICT_IF_TYPE_KNOWN = "objj.resolve.calltarget.FILTER_STRICT_IF_TYPE_KNOWN"
    private const val RESOLVE_CALL_TARGET_FILTER_STRICT_IF_TYPE_KNOWN_DEFAULT = true
    private val filterMethodCallsStrictIfTypeKnownSetting = BooleanSetting(RESOLVE_CALL_TARGET_FILTER_STRICT_IF_TYPE_KNOWN, RESOLVE_CALL_TARGET_FILTER_STRICT_IF_TYPE_KNOWN_DEFAULT)
    var filterMethodCallsStrictIfTypeKnown:Boolean get() {
        return filterMethodCallsStrictIfTypeKnownSetting.value!!
    } set (value) {
        filterMethodCallsStrictIfTypeKnownSetting.value = value
    }

    //CallTarget
    private const val COLLAPSE_BY_DEFAULT_KEY = "objj.resolve.calltarget.COLLAPSE_BY_DEFAULT"
    private const val COLLAPSE_BY_DEFAULT_DEFAULT = false
    private val collapseByDefault = BooleanSetting(COLLAPSE_BY_DEFAULT_KEY, COLLAPSE_BY_DEFAULT_DEFAULT)

    private const val IGNORE_PROPERTIES_KEY = "objj.annotator.ignoreProperties"
    private val ignoredKeywordsSetting = ObjJIgnoredStringsListSetting(IGNORE_PROPERTIES_KEY)

    private const val IGNORE_MISSING_SELECTORS_KEY = "objj.annotator.ignoreMissingSelector"
    private val ignoreMissingSelectorsSetting = ObjJIgnoredStringsListSetting(IGNORE_MISSING_SELECTORS_KEY)

    private const val IGNORE_MISSING_FUNCTIONS_KEY = "objj.annotator.ignoreMissingFunctions"
    private val ignoreMissingFunctionSetting = ObjJIgnoredStringsListSetting(IGNORE_MISSING_FUNCTIONS_KEY)

    private const val IGNORE_OVERSHADOWED_VARIABLES_KEY = "objj.annotator.ignoreOvershadowed"
    private const val IGNORE_OVERSHADOWED_VARIABLES_DEFAULT = false
    private val ignoreOvershadowedVariablesSetting = BooleanSetting(IGNORE_OVERSHADOWED_VARIABLES_KEY, IGNORE_OVERSHADOWED_VARIABLES_DEFAULT)

    private const val IGNORE_UNDERSCORED_CLASSES_KEY = "objj.annotator.ignoreUnderscoredClasses"
    private const val IGNORE_UNDERSCORED_CLASSES_DEFAULT = true
    private val ignoreUnderscoredClassesSetting = BooleanSetting(IGNORE_UNDERSCORED_CLASSES_KEY, IGNORE_UNDERSCORED_CLASSES_DEFAULT)
    var ignoreUnderscoredClasses:Boolean get() {
        return ignoreUnderscoredClassesSetting.value!!
    } set(value) {
        ignoreUnderscoredClassesSetting.value = value
    }

    private const val UNQ_IGNORE_UNDECLARED_VARIABLES_KEY = "objj.annotator.unq_ignore.undeclaredVariables"
    private const val UNQ_IGNORE_UNDECLARED_VARIABLES_DEFAULT = false
    private val unqualifiedIgnoreUndeclaredVariablesSetting = BooleanSetting(UNQ_IGNORE_UNDECLARED_VARIABLES_KEY, UNQ_IGNORE_UNDECLARED_VARIABLES_DEFAULT)
    var unqualifiedIgnore_ignoreUndeclaredVariables:Boolean get() {
        return unqualifiedIgnoreUndeclaredVariablesSetting.value!!
    } set(value) {
        unqualifiedIgnoreUndeclaredVariablesSetting.value = value
    }

    private const val UNQ_IGNORE_METHOD_DECLARATION_KEY = "objj.annotator.unq_ignore.methodDeclaration"
    private const val UNQ_IGNORE_METHOD_DECLARATION_DEFAULT = true
    private val unqualifiedIgnore_ignoreMethodDeclarationSetting = BooleanSetting(UNQ_IGNORE_METHOD_DECLARATION_KEY, UNQ_IGNORE_METHOD_DECLARATION_DEFAULT)
    var unqualifiedIgnore_ignoreMethodDeclaration: Boolean get() {
        return unqualifiedIgnore_ignoreMethodDeclarationSetting.value!!
    } set(value) {
        unqualifiedIgnore_ignoreMethodDeclarationSetting.value = value
    }

    private const val UNQ_IGNORE_CONFLICTION_METHODS_KEY = "objj.annotator.unq_ignore.conflictingMethodDeclaration"
    private const val UNQ_IGNORE_CONFLICTION_METHODS_DEFAULT = false
    private val unqualifiedIgnore_ignoreConflictingMethodDeclarationSetting = BooleanSetting(UNQ_IGNORE_CONFLICTION_METHODS_KEY, UNQ_IGNORE_CONFLICTION_METHODS_DEFAULT)
    var unqualifiedIgnore_ignoreConflictingMethodDeclaration: Boolean get() {
        return unqualifiedIgnore_ignoreConflictingMethodDeclarationSetting.value!!
    } set(value) {
        unqualifiedIgnore_ignoreConflictingMethodDeclarationSetting.value = value
    }

    private const val UNQ_IGNORE_METHOD_RETURN_ERRORS_KEY = "objj.annotator.unq_ignore.ignoreMethodReturnErrors"
    private const val UNQ_IGNORE_METHOD_RETURN_ERRORS_DEFAULT = false
    private val unqualifiedIgnore_methodReturnErrorsSetting = BooleanSetting(UNQ_IGNORE_METHOD_RETURN_ERRORS_KEY, UNQ_IGNORE_METHOD_RETURN_ERRORS_DEFAULT)
    var unqualifiedIgnore_ignoreMethodReturnErrors:Boolean get() {
        return unqualifiedIgnore_methodReturnErrorsSetting.value!!
    } set(value) {
        unqualifiedIgnore_methodReturnErrorsSetting.value = value
    }

    private const val UNQ_IGNORE_INVALID_SELECTOR_ERRORS_KEY = "objj.annotator.unq_ignore.ignoreInvalidSelectorErrors"
    private const val UNQ_IGNORE_INVALID_SELECTOR_ERRORS_DEFAULT = false
    private val unqualifiedIgnore_invalidSelectorErrorsSetting = BooleanSetting(UNQ_IGNORE_INVALID_SELECTOR_ERRORS_KEY, UNQ_IGNORE_INVALID_SELECTOR_ERRORS_DEFAULT)
    var unqualifiedIgnore_ignoreInvalidSelectorErrors:Boolean get() {
        return unqualifiedIgnore_invalidSelectorErrorsSetting.value!!
    } set(value) {
        unqualifiedIgnore_invalidSelectorErrorsSetting.value = value
    }
    private const val UNQ_IGNORE_CLASS_AND_CONTENTS_KEY = "objj.annotator.unq_ignore.ignoreInvalidSelectorErrors"
    private const val UNQ_IGNORE_CLASS_AND_CONTENTS_DEFAULT = true
    private val unqualifiedIgnore_ignoreClassAndContentsSetting = BooleanSetting(UNQ_IGNORE_CLASS_AND_CONTENTS_KEY, UNQ_IGNORE_CLASS_AND_CONTENTS_DEFAULT)
    var unqualifiedIgnore_ignoreClassAndContents:Boolean get() {
        return unqualifiedIgnore_ignoreClassAndContentsSetting.value!!
    } set(value) {
        unqualifiedIgnore_ignoreClassAndContentsSetting.value = value
    }

    fun inferEOS(): Boolean {
        return inferEOS.value ?: INFER_EOS_DEFAULT
    }

    fun inferEos(infer: Boolean) {
        inferEOS.value = infer
    }

    fun validateCallTarget(): Boolean {
        return validateCallTarget.value ?: VALIDATE_CALL_TARGET_DEFAULT
    }

    fun validateCallTarget(newValidateCallTargetValue: Boolean) {
        validateCallTarget.value = newValidateCallTargetValue
    }

    fun collapseByDefault() : Boolean = collapseByDefault.value ?: COLLAPSE_BY_DEFAULT_DEFAULT
    fun collapseByDefault(collapse:Boolean) {
        this.collapseByDefault.value = collapse
    }

    // ============================== //
    // ======= Ignore Variable ====== //
    // ============================== //

    fun ignoreVariableName(keyword:String) = ignoredKeywordsSetting.ignoreKeyword(keyword)

    fun removeIgnoredVariableName(keyword:String) = ignoredKeywordsSetting.removeIgnoredKeyword(keyword)

    fun ignoredVariableNames() : List<String> = ignoredKeywordsSetting.ignoredKeywords()

    fun isIgnoredVariableName(keyword:String) : Boolean  = ignoredKeywordsSetting.isIgnoredKeyword(keyword)

    var ignoredVariableNamesAsString:String get() {
        return ignoredKeywordsSetting.ignoredKeywords().joinToString(ObjJIgnoredStringsListSetting.IGNORE_KEYWORDS_DELIM + " ")
    } set(value) {
        ignoredKeywordsSetting.ignoreKeywords(value)
    }

    // ============================== //
    // ====== Ignore Selector ======= //
    // ============================== //
    fun ignoreSelector(keyword:String) = ignoreMissingSelectorsSetting.ignoreKeyword(keyword)

    fun doNotIgnoreSelector(keyword:String) = ignoreMissingSelectorsSetting.removeIgnoredKeyword(keyword)

    fun ignoredSelectors() : List<String> = ignoreMissingSelectorsSetting.ignoredKeywords()

    var ignoredSelectorsAsString:String get() {
        return ignoreMissingSelectorsSetting.ignoredKeywords().joinToString(ObjJIgnoredStringsListSetting.IGNORE_KEYWORDS_DELIM + " ")
    } set(value) {
        ignoreMissingSelectorsSetting.ignoreKeywords(value)
    }

    fun isIgnoredSelector(keyword:String) : Boolean  = ignoreMissingSelectorsSetting.isIgnoredKeyword(keyword)

    // ============================== //
    // ==== Ignore Function Names === //
    // ============================== //

    fun ignoreFunctionName(keyword:String) = ignoreMissingFunctionSetting.ignoreKeyword(keyword)

    fun removeIgnoredFunctionName(keyword:String) = ignoreMissingFunctionSetting.removeIgnoredKeyword(keyword)

    fun ignoredFunctionNames() : List<String> = ignoreMissingFunctionSetting.ignoredKeywords()

    fun isIgnoredFunctionName(keyword:String) : Boolean  = ignoreMissingFunctionSetting.isIgnoredKeyword(keyword)

    var ignoredFunctionNamesAsString:String get() {
        return ignoreMissingFunctionSetting.ignoredKeywords().joinToString(ObjJIgnoredStringsListSetting.IGNORE_KEYWORDS_DELIM + " ")
    } set(value) {
        ignoreMissingFunctionSetting.ignoreKeywords(value)
    }



    fun ignoreOvershadowedVariables() : Boolean {
        return ignoreOvershadowedVariablesSetting.value ?: IGNORE_OVERSHADOWED_VARIABLES_DEFAULT
    }

    fun ignoreOvershadowedVariables(shouldIgnore:Boolean) {
        ignoreOvershadowedVariablesSetting.value = shouldIgnore
    }



}
