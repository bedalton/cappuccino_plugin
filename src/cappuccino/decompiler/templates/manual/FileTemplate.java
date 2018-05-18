package cappuccino.decompiler.templates.manual;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FileTemplate implements TemplateElement {
    private String fileName;
    private final List<TemplateElement> bodyElements = new ArrayList<>();

    public FileTemplate() {
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void addBodyElement(TemplateElement bodyElement) {
        bodyElements.add(bodyElement);
    }

    public void append(@NotNull String statement) {
        addBodyElement(new StatementTemplate(statement));
    }

    @NotNull
    @Override
    public StringBuilder appendTo(@NotNull final StringBuilder stringBuilder) {
        stringBuilder.append("/*Start File: ").append(fileName).append("*/\n");
        for (TemplateElement bodyElement : bodyElements) {
            bodyElement.appendTo(stringBuilder);
        }
        stringBuilder.append("/* EndFile: ").append(fileName).append(" */");
        return stringBuilder;
    }
}
