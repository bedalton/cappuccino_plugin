package org.cappuccino_project.ide.intellij.plugin.stubs.impl;

import com.intellij.psi.stubs.PsiFileStubImpl;
import org.cappuccino_project.ide.intellij.plugin.lang.ObjJFile;
import org.cappuccino_project.ide.intellij.plugin.stubs.interfaces.ObjJFileStub;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class ObjJFileStubImpl extends PsiFileStubImpl<ObjJFile> implements ObjJFileStub {

    private static final Logger LOGGER = Logger.getLogger("ObjJFileStubImpl");
    private static final Pattern IMPORT_FILENAME_REGEX = Pattern.compile("(.*)?::(.*)");
    private final String fileName;
    private final List<String> imports;

    public ObjJFileStubImpl(ObjJFile objJFile, @NotNull String fileName, @NotNull List<String> imports) {
        super(objJFile);
        this.fileName = fileName;
        this.imports = imports;
    }

    @NotNull
    @Override
    public List<String> getImports() {
        return imports;
    }

    @NotNull
    @Override
    public String getFileName() {
        return fileName;
    }

    @NotNull
    @Override
    public List<String> getImportsForFramework(@NotNull
                                                        String framework) {
        List<String> out = new ArrayList<>();
        MatchResult matchResult;
        String importFramework;
        for (String importString : getImports()) {
            matchResult = IMPORT_FILENAME_REGEX.matcher(importString);
            if (matchResult.groupCount() < 3) {
                LOGGER.log(Level.WARNING, "File import for name is invalid when filtering imports by framework");
                continue;
            }
            importFramework = matchResult.group(1);
            if (importFramework != null && importFramework.equals(framework)) {
                out.add(matchResult.group(2));
            }
        }
        return out;
    }
}
