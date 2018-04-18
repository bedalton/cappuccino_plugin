package org.cappuccino_project.ide.intellij.plugin.psi.utils;

import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJImportStatement;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.FileSystems;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ObjJFilePsiUtil {

    private static final Logger LOGGER = Logger.getLogger(ObjJFilePsiUtil.class.getName());
    private static final String SEPARATOR = FileSystems.getDefault().getSeparator();
    private static final Pattern INFO_PLIST_DICT_PATTERN = Pattern.compile(".*<dict>(.*)</dict>.*");
    private static final Pattern INFO_PLIST_PROPERTY_PATTERN = Pattern.compile("(<key>(.*)</key>\n<[^>]+>(.*)</[^>]+>)*");

    @Nullable
    public static String getContainingFrameworkName(@NotNull final ObjJFile file) {
        final String filePath = file.getVirtualFile().getPath();
        final VirtualFile[] srcRoots = ProjectRootManager.getInstance(file.getProject()).getContentSourceRoots();
        String basePath  = null;
        for (VirtualFile srcRoot : srcRoots) {
            if (filePath.startsWith(srcRoot.getPath())) {
                basePath = srcRoot.getPath();
                break;
            }
        }
        if (basePath == null) {
            LOGGER.log(Level.INFO, "Failed to find base file path for file: <"+filePath+">");
            return null;
        }
        List<String> pathComponents = Arrays.asList(filePath.substring(basePath.length()).split(SEPARATOR));
        VirtualFile plistFile = null;
        while (pathComponents.size() > 0) {
            String path = basePath + "/" + ArrayUtils.join(pathComponents, SEPARATOR,true)+"info.plist";
            LOGGER.log(Level.INFO, "Checking for info.plist at location: <"+path+">");
            plistFile = LocalFileSystem.getInstance().findFileByPath(path);
            if (plistFile != null) {
                break;
            }
            pathComponents.remove(pathComponents.size()-1);
        }
        if (plistFile == null) {
            return null;
        }
        return null;
    }


    private static Map<String,String> getInfoPlistProperties(@Nullable VirtualFile virtualFile) {
        if (virtualFile == null) {
            return Collections.emptyMap();
        }
        return null;
    }

    public static List<String> getImportsAsStrings(@NotNull ObjJFile file) {
        final List<ObjJImportStatement> importStatements = ObjJTreeUtil.getChildrenOfTypeAsList(file, ObjJImportStatement.class);
        if (importStatements.isEmpty()) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        List<String> out = new ArrayList<>();
        for (ObjJImportStatement importStatement : importStatements) {
            out.add(importStatement.getImportAsUnifiedString());
        }
        return out;
    }
}
