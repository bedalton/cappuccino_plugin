package cappuccino.ide.intellij.plugin.jsdef;

import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;

public class JsTypeDefLanguage extends Language {
    public static final JsTypeDefLanguage INSTANCE = new JsTypeDefLanguage();
    public static final String LANGUAGE_NAME = "Objective-J Javascript Type Definition";
    private JsTypeDefLanguage() {
        super("ObjJTypeDef");
    }
}