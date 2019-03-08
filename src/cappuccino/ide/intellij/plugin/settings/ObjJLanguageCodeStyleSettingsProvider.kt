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
        } else if (settingsType == LanguageCodeStyleSettingsProvider.SettingsType.BLANK_LINES_SETTINGS) {
            consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE")
        } else if (settingsType == LanguageCodeStyleSettingsProvider.SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
            consumer.showStandardOptions("FORCE_BRACE_ON_NEW_LINE")
        }
    }

    override fun getCodeSample(settingsType: LanguageCodeStyleSettingsProvider.SettingsType): String? {
        return         return """
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

-(CPString) colorHex
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

        """
    }
}
