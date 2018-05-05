package org.cappuccino_project.ide.intellij.plugin.formatting;

import com.intellij.lang.Language;
import com.intellij.openapi.application.ApplicationBundle;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJLanguage;
import org.jetbrains.annotations.NotNull;

import static com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider.SettingsType.*;

public class ObjJLanguageCodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {
    @NotNull
    @Override
    public Language getLanguage() {
        return ObjJLanguage.INSTANCE;
    }

    @Override
    public void customizeSettings(
            @NotNull
                    CodeStyleSettingsCustomizable consumer,
            @NotNull
                    SettingsType settingsType) {
        if (settingsType == SettingsType.SPACING_SETTINGS) {
            consumer.showStandardOptions("SPACE_AROUND_METHOD_SELECTORS",
                    "SPACE_BEFORE_METHOD_PARAM_TYPE",
                    "SPACE_AFTER_METHOD_PARAM_TYPE"
            );
        } else if (settingsType == BLANK_LINES_SETTINGS) {
            consumer.showStandardOptions(
                    "KEEP_LINE_BREAKS",
                    "KEEP_BLANK_LINES_IN_DECLARATIONS",
                    "KEEP_BLANK_LINES_IN_CODE");
            consumer.showCustomOption(ObjJCodeStyleSettings.class,
                    "MIN_NUMBER_OF_BLANKS_BETWEEN_ITEMS",
                    "Between declarations:",
                    CodeStyleSettingsCustomizable.BLANK_LINES);
        } else if (settingsType == WRAPPING_AND_BRACES_SETTINGS) {
            consumer.showStandardOptions(
                    "RIGHT_MARGIN",
                    "ALIGN_MULTILINE_CHAINED_METHODS",
                    "ALIGN_MULTILINE_PARAMETERS",
                    "ALIGN_MULTILINE_PARAMETERS_IN_CALLS");

            consumer.showCustomOption(ObjJCodeStyleSettings.class,
                    "PRESERVE_PUNCTUATION",
                    "Punctuation",
                    CodeStyleSettingsCustomizable.WRAPPING_KEEP);

            consumer.showCustomOption(ObjJCodeStyleSettings.class,
                    "ALIGN_METHOD_PARAMETERS_BY",
                    "Align multi-line method parameters by:",
                    CodeStyleSettingsCustomizable.WRAPPING_METHOD_PARAMETERS,
                    "Semi-Colon", "Selector Start", "Parameter End");

            consumer.showCustomOption(ObjJCodeStyleSettings.class,
                    "ALIGN_TYPE_PARAMS",
                    ApplicationBundle.message("wrapping.align.when.multiline"),
                    "Type parameters");
            consumer.showCustomOption(ObjJCodeStyleSettings.class,
                    "ALIGN_BLOCK_BRACE_ON_NEXT_LINE",
                    "Place block brace on new line",
                    CodeStyleSettingsCustomizable.WRAPPING_BRACES
            );
        }

    }


    @Override
    public CommonCodeStyleSettings getDefaultCommonSettings() {
        CommonCodeStyleSettings defaultSettings = new CommonCodeStyleSettings(getLanguage());
        CommonCodeStyleSettings.IndentOptions indentOptions = defaultSettings.initIndentOptions();
        indentOptions.INDENT_SIZE = 4;
        indentOptions.CONTINUATION_INDENT_SIZE = 8;
        indentOptions.TAB_SIZE = 4;
        indentOptions.USE_TAB_CHARACTER = false;

        defaultSettings.LINE_COMMENT_AT_FIRST_COLUMN = false;
        defaultSettings.KEEP_FIRST_COLUMN_COMMENT = false;

        return defaultSettings;
    }


    @Override
    public String getCodeSample(
            @NotNull
                    SettingsType settingsType) {
        if (settingsType == WRAPPING_AND_BRACES_SETTINGS) {
            return WRAPPING_AND_BRACES_SAMPLE;
        } else if (settingsType == INDENT_SETTINGS) {
            return INDENT_SETTINGS_SAMPLE;
        } else if (settingsType == SPACING_SETTINGS) {
            return SPACING_SAMPLE;
        } else {
            return "";
        }
        /*
        return "@import <Framework/Framework.j>" + "\n" +
                "@import \"Person.j\"" + "\n" +
                "\n" +
                "#define preprocessorFunction (aVariable)\\\n" +
                "    if (aVariable == @\"expectedValue\")\\\n" +
                "        return YES;\\\n" +
                "    else\\\n" +
                "        return NO;\n" +
                "\n" +
                "\n" +
                "@implementation BreakfastShack : CPObject <Restaurant, IsCheap>" + "\n" +
                "{\n" +
                "    CPString        _languageName @accessors(property=active);\n" +
                "    BOOL            _active;\n" +
                "}\n" +
                "\n" +
                "-(BOOL)setBreakfast:(CPString)breakfast forPeople:(Person)people withPrice:aPrice\n" +
                "{\n" +
                "    var numberOfPeople = people.length\n" +
                "        isValidBreakfast = isValidBreakfast(breakfast, people);\n" +
                "\n" +
                "    if (!isValidBreakfast)\n" +
                "        return NO;\n" +
                "\n" +
                "        var personsName,\n" +
                "            oldBreakfast;\n" +
                "\n" +
                "        for (var person in people)\n" +
                "        {\n" +
                "            personsName = [person name];\n" +
                "            oldBreakfast = [person breakfast]\n" +
                "\n" +
                "            if (breakfast != oldBreakfast)\n" +
                "            {\n" +
                "\n" +
                "                [person setMeal:breakfast forMeal:@\"breakfast\" withPrice:aPrice];\n" +
                "                [self person:personsName " +
                "                     forMeal:@\"breakfast\"];\n" +
                "\n" +
                "            }\n" +
                "\n" +
                "        }\n" +
                "    return YES;\n" +
                "\n" +
                "}\n" +
                "\n" +
                "\n" +
                "\n" +
                "function isValidBreakfast(breakfast, people){\n" +
                "\n" +
                "    for (int i=0; i<people.length; i++)\n" +
                "    {\n" +
                "        var person = people[i]\n" +
                "\n" +
                "        if (![person canEat:breakfast])\n" +
                "            return NO;\n" +
                "\n" +
                "    }\n" +
                "        return YES\n" +
                "    }\n" +
                "\n" +
                "}\n" +
                "@end"
                ;*/
    }

    private static final String WRAPPING_AND_BRACES_SAMPLE =
            "#define preprocessorFunction (aVariable)\\\n" +
                    "    if (aVariable == @\"expectedValue\")\\\n" +
                    "        return YES;\\\n" +
                    "    else\\\n" +
                    "        return NO;\n" +
                    "\n" +
                    "\n" +
                    "@implementation NewClass\n" +
                    "{\n" +
                    "    CPString sampleText;\n" +
                    "}\n" +
                    "-(BOOL)setBreakfast:(CPString)breakfast\n" +
                    "          forPeople:(Person)people\n" +
                    "          withPrice:aPrice\n" +
                    "{\n" +
                    "    function (variable1, variable2,\n" +
                    "                         variable3)\n" +
                    "    {\n" +
                    "        var listVar1,\n" +
                    "            listVar2,\n" +
                    "            listVar3;\n" +
                    "\n" +
                    "        [self selector1:aValue, useValue:YES" +
                    "                       anotherSelector:@\"Another Value\"]\n" +
                    "\n" +
                    "        if (variable3.indexOf(listVar1) >= 0)\n" +
                    "        {\n" +
                    "            return YES;\n" +
                    "        }\n" +
                    "        else\n" +
                    "        {\n" +
                    "            return NO;\n" +
                    "        }\n" +
                    "\n" +
                    "}\n";

    private static final String INDENT_SETTINGS_SAMPLE = WRAPPING_AND_BRACES_SAMPLE;

    private static final String SPACING_SAMPLE =
            "" +
                    "#if PLATFORM(DOM)\n" +
                    "    return YES;\n" +
                    "\n" +
                    "if (aValue)\n" +
                    "{\n" +
                    "   return aValue\n" +
                    "}\n" +
                    "\n" +
                    "function aFunction(aValue)\n" +
                    "{\n" +
                    "    \n" +
                    "}\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "\n" +
                    "\n";
}
