package cappuccino.ide.intellij.plugin.inspections;

import com.intellij.codeInspection.InspectionToolProvider;
import org.jetbrains.annotations.NotNull;

public class ObjJInspectionProvider implements InspectionToolProvider {
    public static final String GROUP_DISPLAY_NAME = "Objective-J";
    @NotNull
    @Override
    public Class[] getInspectionClasses() {
        return new Class[] {
            ObjJUndeclaredVariableInspectionTool.class,
                ObjJVariableKeyWordScopeInspection.class,
                ObjJVariableOvershadowInspection.class
        };
    }
}
