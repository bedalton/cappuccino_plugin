package cappuccino.ide.intellij.plugin.lang;

import com.intellij.lang.Language;

public class ObjJLanguage extends Language {
    public static final ObjJLanguage INSTANCE = new ObjJLanguage();

    private ObjJLanguage() {
        super("ObjectiveJ");
    }
}
