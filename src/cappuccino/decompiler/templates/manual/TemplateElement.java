package cappuccino.decompiler.templates.manual;

import org.jetbrains.annotations.NotNull;

public interface TemplateElement {
    @NotNull
    StringBuilder appendTo(
            @NotNull
            final StringBuilder stringBuilder);
}
