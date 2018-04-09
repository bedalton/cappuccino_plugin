package org.cappuccino_project.ide.intellij.plugin.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ObjJFileUtil {

    private static final List<ObjJFile> EMPTY_FILE_LIST = Collections.emptyList();

    /**
     * Gets a list of PSI files for the names of strings given
     * Todo: actually get imported files.
     * @param importedFileNames list of file names specified in import statements
     * @param project project to get files from
     * @return files for import file names
     */
    public List<ObjJFile> getImportedFiles(List<String> importedFileNames, @NotNull Project project) {
        return EMPTY_FILE_LIST;
    }

    @Nullable
    public static String getContainingFileName(@Nullable PsiElement psiElement) {
        return getFileNameSafe(psiElement != null ? psiElement.getContainingFile() : null);
    }

    @Nullable
    public static String getFileNameSafe(@Nullable PsiFile psiFile) {
        return getFileNameSafe(psiFile, null);
    }

    @Nullable
    public static String getFileNameSafe(@Nullable PsiFile psiFile, @Nullable String defaultValue) {
        return getFileNameSafe(psiFile, defaultValue, false);
    }

    @Nullable
    public static String getFileNameSafe(@Nullable PsiFile psiFile, @Nullable String defaultValue, boolean includePath) {
        if (psiFile == null) {
            return defaultValue;
        }
        if (psiFile.getVirtualFile() == null) {
            return defaultValue;
        }
        if (includePath) {
            return psiFile.getVirtualFile().getPath();
        }
        return psiFile.getVirtualFile().getName();
    }

}
