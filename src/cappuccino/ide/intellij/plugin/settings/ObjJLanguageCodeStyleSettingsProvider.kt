package cappuccino.ide.intellij.plugin.settings

import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider

class ObjJLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {

    override fun getLanguage(): Language {
        return ObjJLanguage.instance
    }

    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: LanguageCodeStyleSettingsProvider.SettingsType) {
        if (settingsType == LanguageCodeStyleSettingsProvider.SettingsType.SPACING_SETTINGS) {
            consumer.showStandardOptions("SPACE_AROUND_ASSIGNMENT_OPERATORS")
            consumer.renameStandardOption("SPACE_AROUND_ASSIGNMENT_OPERATORS", "Separator")
            consumer.showStandardOptions("SPACE_AFTER_COMMA")
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "SPACE_BETWEEN_SELECTOR_AND_VARIABLE_TYPE", "Space Between Selector and Type", "Method Declaration")
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "SPACE_BETWEEN_VARIABLE_TYPE_AND_NAME", "Space After Variable Type", "Method Declaration")
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "SPACE_BETWEEN_RETURN_TYPE_AND_FIRST_SELECTOR", "Space After Return Type", "Method Declaration")
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "SPACE_BETWEEN_TYPE_AND_PARENS", "Space Between Type and Parenthesis", "Method Declaration")
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "ALIGN_SELECTORS_IN_METHOD_DECLARATION", "Align Method Declaration Selector Colons", "Method Declaration")
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "SPACE_BETWEEN_SELECTOR_AND_VALUE_IN_METHOD_CALL", "Space Between Selector and Value", "Method Call")
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "ALIGN_SELECTORS_IN_METHOD_CALL", "Align Method Call Selector Colons", "Method Call")
            consumer.showCustomOption(ObjJCodeStyleSettings::class.java, "SPACE_BEFORE_PAREN_STATEMENT", "Space Between control keyword and paren statement", "General")
        } else if (settingsType == LanguageCodeStyleSettingsProvider.SettingsType.BLANK_LINES_SETTINGS) {
            consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE")
        } else if (settingsType == LanguageCodeStyleSettingsProvider.SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
            consumer.showStandardOptions("FORCE_BRACE_ON_NEW_LINE")
            consumer.showStandardOptions("SPACE_BEFORE_LBRACE")
        }
    }

    override fun getCodeSample(settingsType: SettingsType): String? {
    return """
@import <Foundation/Foundation.j>
#include "SomeClass.j"

/*
    A Block Comment
*/
@implementation MyClass : <HasProtocol>
{
    id _reference;
    int _varInt;
    CPString _string  @accessors(property=string);
    CPColor _backgroundColor;
}

-(void) setBackgroundColor:(CPColor)aColor
{
    //A Line Comment
    var i = 0;

    i++
    _backgroundColor = aColor;
}

-(void) setBackgroundColor:(CPColor)aColor
forType:(DomType)clazz
withFilter:(Function)type
{
    //A Line Comment
    var i = 0;

    i++
    _backgroundColor = aColor;
}

-(CPString)colorHex
{
    if (_backgroundColor)
    {
        return [_backgroundColor hexString];
    }
    else
    {
        return Nil
    }
}

-(void)method2
{
    [self setBackgroundColor:_backgroundColor
    forType:[DomType anchor]
    withFilter: _domFilterFunction];
}


@end

#pragma mark - Greeting Functions

//This is a global variable
globalGreeting = @"Hello %s!";

//This is a regular variable
var element = document.getElementById("tagName");

element.innerHTML= @"21";

var isValidName = function(aName)
{
    if (aName)
    {
        return YES;
    }
    else
    {
        return NO;
    }
}

function sayHello(aName, var2)
{
    if (isValidName(aName, var2))
    {
        return [CPString stringWithFormat:globalGreeting, aName, var2];
    }
    return null;
}

        """.trimIndent()
    }
}
