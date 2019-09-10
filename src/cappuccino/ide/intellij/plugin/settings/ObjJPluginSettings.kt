@file:Suppress("unused")

package cappuccino.ide.intellij.plugin.settings

import cappuccino.ide.intellij.plugin.jstypedef.stubs.JsTypeDefStubVersion
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.BooleanSetting
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.IntegerSetting
import cappuccino.ide.intellij.plugin.settings.ObjJPluginSettingsUtil.StringSetting
import cappuccino.ide.intellij.plugin.stubs.ObjJStubVersions
import cappuccino.ide.intellij.plugin.utils.PLUGIN_VERSION
import cappuccino.ide.intellij.plugin.utils.orDefault
import cappuccino.ide.intellij.plugin.utils.orFalse

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
    var resolveCallTargetFromAssignments: Boolean
        get() {
            return resolveCallTargetSetting.value!!
        }
        set(value) {
            resolveCallTargetSetting.value = value
        }

    private const val RESOLVE_CALL_TARGET_FILTER_STRICT_IF_TYPE_KNOWN = "objj.resolve.calltarget.FILTER_STRICT_IF_TYPE_KNOWN"
    private const val RESOLVE_CALL_TARGET_FILTER_STRICT_IF_TYPE_KNOWN_DEFAULT = true
    private val filterMethodCallsStrictIfTypeKnownSetting = BooleanSetting(RESOLVE_CALL_TARGET_FILTER_STRICT_IF_TYPE_KNOWN, RESOLVE_CALL_TARGET_FILTER_STRICT_IF_TYPE_KNOWN_DEFAULT)
    var filterMethodCallsStrictIfTypeKnown: Boolean
        get() {
            return filterMethodCallsStrictIfTypeKnownSetting.value!!
        }
        set(value) {
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
    var ignoreUnderscoredClasses: Boolean
        get() {
            return ignoreUnderscoredClassesSetting.value.orDefault(IGNORE_UNDERSCORED_CLASSES_DEFAULT)
        }
        set(value) {
            ignoreUnderscoredClassesSetting.value = value
        }

    private const val IGNORE_MISSING_CLASSES_WHEN_SUFFIXED_WITH_REF_OR_POINTER = "objj.inspection.ignoreMissingClassesWhenSuffixedWithRefOrPointer"
    private const val IGNORE_MISSING_CLASSES_WHEN_SUFFIXED_WITH_REF_OR_POINTER_DEFAULT = false
    private val ignoreMissingClassesWhenSuffixedWithRefOrPointerSetting = BooleanSetting(IGNORE_MISSING_CLASSES_WHEN_SUFFIXED_WITH_REF_OR_POINTER, IGNORE_MISSING_CLASSES_WHEN_SUFFIXED_WITH_REF_OR_POINTER_DEFAULT)
    var ignoreMissingClassesWhenSuffixedWithRefOrPointer: Boolean
        get() {
            return ignoreMissingClassesWhenSuffixedWithRefOrPointerSetting.value.orDefault(IGNORE_MISSING_CLASSES_WHEN_SUFFIXED_WITH_REF_OR_POINTER_DEFAULT)
        }
        set(value) {
            ignoreMissingClassesWhenSuffixedWithRefOrPointerSetting.value = value
        }

    private const val UNQ_IGNORE_UNDECLARED_VARIABLES_KEY = "objj.annotator.unq_ignore.undeclaredVariables"
    private const val UNQ_IGNORE_UNDECLARED_VARIABLES_DEFAULT = false
    private val unqualifiedIgnoreUndeclaredVariablesSetting = BooleanSetting(UNQ_IGNORE_UNDECLARED_VARIABLES_KEY, UNQ_IGNORE_UNDECLARED_VARIABLES_DEFAULT)
    var unqualifiedIgnore_ignoreUndeclaredVariables: Boolean
        get() {
            return unqualifiedIgnoreUndeclaredVariablesSetting.value!!
        }
        set(value) {
            unqualifiedIgnoreUndeclaredVariablesSetting.value = value
        }

    private const val UNQ_IGNORE_METHOD_DECLARATION_KEY = "objj.annotator.unq_ignore.methodDeclaration"
    private const val UNQ_IGNORE_METHOD_DECLARATION_DEFAULT = true
    private val unqualifiedIgnore_ignoreMethodDeclarationSetting = BooleanSetting(UNQ_IGNORE_METHOD_DECLARATION_KEY, UNQ_IGNORE_METHOD_DECLARATION_DEFAULT)
    var unqualifiedIgnore_ignoreMethodDeclaration: Boolean
        get() {
            return unqualifiedIgnore_ignoreMethodDeclarationSetting.value!!
        }
        set(value) {
            unqualifiedIgnore_ignoreMethodDeclarationSetting.value = value
        }

    private const val UNQ_IGNORE_CONFLICTION_METHODS_KEY = "objj.annotator.unq_ignore.conflictingMethodDeclaration"
    private const val UNQ_IGNORE_CONFLICTION_METHODS_DEFAULT = false
    private val unqualifiedIgnore_ignoreConflictingMethodDeclarationSetting = BooleanSetting(UNQ_IGNORE_CONFLICTION_METHODS_KEY, UNQ_IGNORE_CONFLICTION_METHODS_DEFAULT)
    var unqualifiedIgnore_ignoreConflictingMethodDeclaration: Boolean
        get() {
            return unqualifiedIgnore_ignoreConflictingMethodDeclarationSetting.value!!
        }
        set(value) {
            unqualifiedIgnore_ignoreConflictingMethodDeclarationSetting.value = value
        }

    private const val UNQ_IGNORE_METHOD_RETURN_ERRORS_KEY = "objj.annotator.unq_ignore.ignoreMethodReturnErrors"
    private const val UNQ_IGNORE_METHOD_RETURN_ERRORS_DEFAULT = false
    private val unqualifiedIgnore_methodReturnErrorsSetting = BooleanSetting(UNQ_IGNORE_METHOD_RETURN_ERRORS_KEY, UNQ_IGNORE_METHOD_RETURN_ERRORS_DEFAULT)
    var unqualifiedIgnore_ignoreMethodReturnErrors: Boolean
        get() {
            return unqualifiedIgnore_methodReturnErrorsSetting.value!!
        }
        set(value) {
            unqualifiedIgnore_methodReturnErrorsSetting.value = value
        }

    private const val VALIDATE_METHOD_CALLS_KEY = "objj.inspections.validateMethodCalls"
    private const val VALIDATE_METHOD_CALLS_KEY_DEFAULT = false
    private val validateMethodCallsSetting = BooleanSetting(UNQ_IGNORE_METHOD_RETURN_ERRORS_KEY, UNQ_IGNORE_METHOD_RETURN_ERRORS_DEFAULT)
    var validateMethodCalls: Boolean
        get() {
            return validateMethodCallsSetting.value!!
        }
        set(value) {
            validateMethodCallsSetting.value = value
        }

    private const val UNQ_IGNORE_INVALID_SELECTOR_ERRORS_KEY = "objj.annotator.unq_ignore.ignoreInvalidSelectorErrors"
    private const val UNQ_IGNORE_INVALID_SELECTOR_ERRORS_DEFAULT = false
    private val unqualifiedIgnore_invalidSelectorErrorsSetting = BooleanSetting(UNQ_IGNORE_INVALID_SELECTOR_ERRORS_KEY, UNQ_IGNORE_INVALID_SELECTOR_ERRORS_DEFAULT)
    var unqualifiedIgnore_ignoreInvalidSelectorErrors: Boolean
        get() {
            return unqualifiedIgnore_invalidSelectorErrorsSetting.value!!
        }
        set(value) {
            unqualifiedIgnore_invalidSelectorErrorsSetting.value = value
        }
    private const val UNQ_IGNORE_CLASS_AND_CONTENTS_KEY = "objj.annotator.unq_ignore.ignoreInvalidSelectorErrors"
    private const val UNQ_IGNORE_CLASS_AND_CONTENTS_DEFAULT = true
    private val unqualifiedIgnore_ignoreClassAndContentsSetting = BooleanSetting(UNQ_IGNORE_CLASS_AND_CONTENTS_KEY, UNQ_IGNORE_CLASS_AND_CONTENTS_DEFAULT)
    var unqualifiedIgnore_ignoreClassAndContents: Boolean
        get() {
            return unqualifiedIgnore_ignoreClassAndContentsSetting.value!!
        }
        set(value) {
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

    fun collapseByDefault(): Boolean = collapseByDefault.value ?: COLLAPSE_BY_DEFAULT_DEFAULT
    fun collapseByDefault(collapse: Boolean) {
        this.collapseByDefault.value = collapse
    }

    // ============================== //
    // ======= Ignore Variable ====== //
    // ============================== //

    fun ignoreVariableName(keyword: String) = ignoredKeywordsSetting.ignoreKeyword(keyword)

    fun removeIgnoredVariableName(keyword: String) = ignoredKeywordsSetting.removeIgnoredKeyword(keyword)

    fun ignoredVariableNames(): List<String> = ignoredKeywordsSetting.ignoredKeywords()

    fun isIgnoredVariableName(keyword: String): Boolean = ignoredKeywordsSetting.isIgnoredKeyword(keyword)

    var ignoredVariableNamesAsString: String
        get() {
            return ignoredKeywordsSetting.ignoredKeywords().joinToString(ObjJIgnoredStringsListSetting.IGNORE_KEYWORDS_DELIM + " ")
        }
        set(value) {
            ignoredKeywordsSetting.ignoreKeywords(value)
        }

    // ============================== //
    // ====== Ignore Selector ======= //
    // ============================== //
    fun ignoreSelector(keyword: String) = ignoreMissingSelectorsSetting.ignoreKeyword(keyword)

    fun doNotIgnoreSelector(keyword: String) = ignoreMissingSelectorsSetting.removeIgnoredKeyword(keyword)

    fun ignoredSelectors(): List<String> = ignoreMissingSelectorsSetting.ignoredKeywords()

    var ignoredSelectorsAsString: String
        get() {
            return ignoreMissingSelectorsSetting.ignoredKeywords().joinToString(ObjJIgnoredStringsListSetting.IGNORE_KEYWORDS_DELIM + " ")
        }
        set(value) {
            ignoreMissingSelectorsSetting.ignoreKeywords(value)
        }

    fun isIgnoredSelector(keyword: String): Boolean = ignoreMissingSelectorsSetting.isIgnoredKeyword(keyword)

    // ============================== //
    // ==== Ignore Function Names === //
    // ============================== //

    fun ignoreFunctionName(keyword: String) = ignoreMissingFunctionSetting.ignoreKeyword(keyword)

    fun removeIgnoredFunctionName(keyword: String) = ignoreMissingFunctionSetting.removeIgnoredKeyword(keyword)

    fun ignoredFunctionNames(): List<String> = ignoreMissingFunctionSetting.ignoredKeywords()

    fun isIgnoredFunctionName(keyword: String): Boolean = ignoreMissingFunctionSetting.isIgnoredKeyword(keyword)

    var ignoredFunctionNamesAsString: String
        get() {
            return ignoreMissingFunctionSetting.ignoredKeywords().joinToString(ObjJIgnoredStringsListSetting.IGNORE_KEYWORDS_DELIM + " ")
        }
        set(value) {
            ignoreMissingFunctionSetting.ignoreKeywords(value)
        }

    // ============================== //
    // ===== Ignore Class Names ===== //
    // ============================== //
    private const val IGNORE_MISSING_CLASSES_KEY = "objj.annotator.ignoreMissingFunctions"
    private val ignoreMissingClassNamesSetting = ObjJIgnoredStringsListSetting(IGNORE_MISSING_CLASSES_KEY)


    fun ignoreClassName(keyword: String) = ignoreMissingClassNamesSetting.ignoreKeyword(keyword)

    fun removeIgnoredClassName(keyword: String) = ignoreMissingClassNamesSetting.removeIgnoredKeyword(keyword)

    fun ignoredClassNames(): List<String> = ignoreMissingClassNamesSetting.ignoredKeywords()

    fun isIgnoredClassName(keyword: String): Boolean = ignoreMissingClassNamesSetting.isIgnoredKeyword(keyword)

    var ignoredClassNamesAsString: String
        get() {
            return ignoreMissingClassNamesSetting.ignoredKeywords().joinToString(ObjJIgnoredStringsListSetting.IGNORE_KEYWORDS_DELIM + " ")
        }
        set(value) {
            ignoreMissingClassNamesSetting.ignoreKeywords(value)
        }

    fun ignoreOvershadowedVariables(): Boolean {
        return ignoreOvershadowedVariablesSetting.value ?: IGNORE_OVERSHADOWED_VARIABLES_DEFAULT
    }

    fun ignoreOvershadowedVariables(shouldIgnore: Boolean) {
        ignoreOvershadowedVariablesSetting.value = shouldIgnore
    }

    // ============================== //
    // ======== Experimental ======== //
    // ============================== //
    private val experimental_allowSelectorRenameSetting: BooleanSetting = BooleanSetting("objj.experimental.allowSelectorRename", false)
    var experimental_allowSelectorRename: Boolean
        get() {
            return experimental_allowSelectorRenameSetting.value.orFalse()
        }
        set(value) {
            experimental_allowSelectorRenameSetting.value = value
        }
    val experimental_didAskAboutAllowSelectorRenameSetting: BooleanSetting = BooleanSetting("objj.experimental.didAskAboutAllowSelectorRename", false)
    var experimental_didAskAboutAllowSelectorRename: Boolean
        get() {
            return experimental_didAskAboutAllowSelectorRenameSetting.value.orFalse()
        }
        set(value) {
            experimental_didAskAboutAllowSelectorRenameSetting.value = value
        }
    private val experimental_allowFrameworkSelectorRenameSetting: BooleanSetting = BooleanSetting("objj.experimental.allowFrameworkSelectorRename", false)
    var experimental_allowFrameworkSelectorRename: Boolean
        get() {
            return experimental_allowFrameworkSelectorRenameSetting.value.orFalse()
        }
        set(value) {
            experimental_allowFrameworkSelectorRenameSetting.value = value
        }

    private val messages_didSend_stubVersionSetting: IntegerSetting = IntegerSetting("objj.messages.restart-normally-after-index-update.did-send", -1)
    var stubVersionsUpdated: Boolean
        get()
        = messages_didSend_stubVersionSetting.value != ObjJStubVersions.SOURCE_STUB_VERSION + JsTypeDefStubVersion.VERSION + ObjJStubVersions.SOURCE_STUB_VERSION
        set(value) {
            if (!value) {
                messages_didSend_stubVersionSetting.value = ObjJStubVersions.SOURCE_STUB_VERSION + JsTypeDefStubVersion.VERSION + ObjJStubVersions.SOURCE_STUB_VERSION
            } else {
                messages_didSend_stubVersionSetting.value = -1
            }
        }
    private val typedefVersionSetting: StringSetting = StringSetting("jstypedef.version", "")
    var typedefVersion: String
        get()
        = typedefVersionSetting.value ?: ""
        set(value) {
            typedefVersionSetting.value = value
        }

    private val lastLoadedVersionSetting: StringSetting = StringSetting("objj.messages.restart-normally-after-index-update.did-send", "")
    var pluginUpdated: Boolean
        get() = lastLoadedVersionSetting.value != PLUGIN_VERSION
    set(wasUpdated) {
        if (wasUpdated) {
            lastLoadedVersionSetting.value = ""
        } else
            lastLoadedVersionSetting.value = PLUGIN_VERSION
    }

}
