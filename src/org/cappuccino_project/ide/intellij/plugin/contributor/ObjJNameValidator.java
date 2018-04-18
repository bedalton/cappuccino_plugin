package org.cappuccino_project.ide.intellij.plugin.contributor;

import com.intellij.lang.refactoring.NamesValidator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ObjJNameValidator implements NamesValidator {


    private static final Pattern validNamePattern = Pattern.compile("(^[a-zA-Z0-9_$]*)$");

    @Override
    public boolean isKeyword(
            @NotNull
                    String string, Project project) {
        return ObjJKeywordsList.keywords.indexOf(string) >= 0 || !validNamePattern.matcher(string).matches();
    }

    @Override
    public boolean isIdentifier(
            @NotNull
                    String string, Project project) {
        return validNamePattern.matcher(string).matches();
    }
}
