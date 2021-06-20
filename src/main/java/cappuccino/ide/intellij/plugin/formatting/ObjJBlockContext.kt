package cappuccino.ide.intellij.plugin.formatting

import cappuccino.ide.intellij.plugin.lang.ObjJLanguage
import com.intellij.formatting.FormattingMode
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings

class ObjJBlockContext(val settings: CodeStyleSettings, val mode: FormattingMode) {
    val objJSettings: CommonCodeStyleSettings = settings.getCommonSettings(ObjJLanguage.instance)

}