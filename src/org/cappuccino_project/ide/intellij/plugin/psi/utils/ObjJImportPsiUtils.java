package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import org.cappuccino_project.ide.intellij.plugin.psi.*;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJImportStub;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class ObjJImportPsiUtils {
    private static final Pattern FRAMEWORK_REGEX = Pattern.compile("<(.*)/(.*)>");

    @NotNull
    public static String getFileName (@NotNull
                                                 ObjJImportFile importStatement) {
        if (importStatement.getStub() != null) {
            return importStatement.getStub().getFileName();
        }
        return importStatement.getStringLiteral().getStringValue();
    }

    @NotNull
    public static String getFileName (@NotNull
                                              ObjJIncludeFile includeFile) {
        if (includeFile.getStub() != null) {
            return includeFile.getStub().getFileName();
        }
        return includeFile.getStringLiteral().getStringValue();
    }

    @NotNull
    public static String getFileName(@NotNull ObjJImportFramework statement) {
        if (statement.getStub() != null) {
            return statement.getStub().getFileName();
        }
        return statement.getFrameworkReference().getFileName();
    }

    @NotNull
    public static String getFileName(@NotNull
                                             ObjJIncludeFramework statement) {
        if (statement.getStub() != null) {
            return statement.getStub().getFileName();
        }
        return statement.getFrameworkReference().getFileName();
    }

    @NotNull
    public static String getFileName(@NotNull
                                             ObjJFrameworkReference reference) {
        MatchResult matchResult = FRAMEWORK_REGEX.matcher(reference.getImportFrameworkLiteral().getText());
        if (matchResult.groupCount() < 3) {
            return "";
        }
        return matchResult.group(2);
    }


    @Nullable
    public static String getFrameworkName(@NotNull ObjJIncludeFile ignored) {
        return null;
    }

    @Nullable
    public static String getFrameworkName(@NotNull
                                                 ObjJImportFile ignored) {
        return null;
    }

    @Nullable
    public static String getFrameworkName(@NotNull ObjJImportFramework framework) {
        if (framework.getStub() != null) {
            return framework.getStub().getFramework();
        }
        return getFrameworkName(framework.getFrameworkReference());
    }

    @Nullable
    public static String getFrameworkName(@NotNull ObjJIncludeFramework framework) {
        if (framework.getStub() != null) {
            return framework.getStub().getFramework();
        }
        return getFrameworkName(framework.getFrameworkReference());
    }

    @Nullable
    public static String getFrameworkName(@NotNull ObjJFrameworkReference reference) {
        MatchResult matchResult = FRAMEWORK_REGEX.matcher(reference.getImportFrameworkLiteral().getText());
        if (matchResult.groupCount() < 3) {
            return null;
        }
        return matchResult.group(1);
    }


}
