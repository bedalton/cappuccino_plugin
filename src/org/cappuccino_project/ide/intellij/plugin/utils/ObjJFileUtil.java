package org.cappuccino_project.ide.intellij.plugin.utils;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJImportStatement;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ObjJFileUtil {

    private static final List<ObjJFile> EMPTY_FILE_LIST = Collections.emptyList();
    public static final String FILE_PATH_KEY = "__FILE__";

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
        if (psiFile.getVirtualFile() != null) {
            if (includePath) {
                return psiFile.getVirtualFile().getPath();
            }
            return psiFile.getVirtualFile().getName();
        }
        String fileName = psiFile.getOriginalFile().getName();
        return !fileName.isEmpty() ? fileName : defaultValue;
    }

    @Nullable
    public static String getFilePath(@Nullable PsiFile psiFile, @Nullable String defaultValue) {
        if (psiFile == null) {
            return defaultValue;
        }
        if (psiFile.getVirtualFile() != null) {
            return psiFile.getVirtualFile().getPath();
        }
        try {
            return psiFile.getOriginalFile().getVirtualFile().getPath();
        } catch (Exception ignored) {}
        return defaultValue;
    }

    public Map<String, List<String>> getImportsAsMap(@NotNull PsiFile file) {
        Map<String, List<String>> out = new HashMap<>();
        getImportsAsMap(file, out);
        return out;
    }

    private void getImportsAsMap(@NotNull PsiFile file, Map<String, List<String>> imports) {
        List<ObjJImportStatement> importStatements = ObjJTreeUtil.getChildrenOfTypeAsList(file, ObjJImportStatement.class);
        List<String> checked = new ArrayList<>();
        final Project project = file.getProject();
        final GlobalSearchScope searchScope = GlobalSearchScope.everythingScope(project);
        for (ObjJImportStatement importStatement : importStatements) {
            final String framework = importStatement.getFrameworkName();
            final String fileName = importStatement.getFileName();
            if (!addImport(imports, framework, fileName)) {
                continue;
            }
            PsiFile[] possibleFiles = FilenameIndex.getFilesByName(project, fileName, searchScope);
            for (PsiFile possibleImportedFile : possibleFiles) {
                if (framework != null && !framework.isEmpty()) {
                    PsiDirectory directory = possibleImportedFile.getContainingDirectory();
                    while (directory != null) {
                        String directoryName = directory.getName();
                        if (directoryName.equals(framework)) {
                            getImportsAsMap(possibleImportedFile, imports);
                            directory = null;
                        } else {
                            directory = directory.getParentDirectory();
                        }
                    }
                    break;
                }
                if (possibleImportedFile.getContainingDirectory().isEquivalentTo(file.getContainingDirectory())) {
                    getImportsAsMap(possibleImportedFile, imports);
                    break;
                }
            }
        }
    }

    public boolean addImport(@NotNull Map<String, List<String>> imports,
                             @Nullable String framework,
                             @NotNull String fileName
    ) {
        if (framework == null || framework.isEmpty()) {
            framework = FILE_PATH_KEY;
        }
        List<String> files = imports.containsKey(framework) ? imports.get(framework) : new ArrayList<>();
        if (files.contains(fileName)) {
            return false;
        }
        files.add(fileName);
        imports.put(framework, files);
        return true;
    }

    public boolean inList(@NotNull ObjJFile file, @NotNull List<Pattern> filePaths) {
        final String thisPath = file.getVirtualFile().getPath();
        for (Pattern pattern : filePaths) {
            if (pattern.matcher(thisPath).matches()) {
                return true;
            }
        }
        return false;
    }

}
