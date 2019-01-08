package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import com.intellij.formatting.FormattingMode
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings

internal class ObjJBlockContext(val settings: CodeStyleSettings, val mode: FormattingMode) {
    val dartSettings: CommonCodeStyleSettings = settings.getCommonSettings(ObjJLanguage.instance)

}