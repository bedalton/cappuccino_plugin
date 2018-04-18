package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndexKey;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJBlock;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJVariableName;
import org.cappuccino_project.ide.intellij.plugin.utils.ObjJFileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ObjJVariableNameByScopeIndex extends ObjJStringStubIndexBase<ObjJVariableName> {
    private static final ObjJVariableNameByScopeIndex INSTANCE = new ObjJVariableNameByScopeIndex();
    public static final StubIndexKey<String, ObjJVariableName> KEY = IndexKeyUtil.createIndexKey(ObjJVariableNameByScopeIndex.class);
    private static final String KEY_FORMAT = "%s-%s-%s";
    private static final int VERSION = 1;


    private ObjJVariableNameByScopeIndex(){}

    public static ObjJVariableNameByScopeIndex getInstance() {
        return INSTANCE;
    }

    @Override
    public int getVersion() {
        return super.getVersion() + VERSION;
    }

    @NotNull
    @Override
    protected Class<ObjJVariableName> getIndexedElementClass() {
        return ObjJVariableName.class;
    }

    @NotNull
    @Override
    public StubIndexKey<String, ObjJVariableName> getKey() {
        return KEY;
    }

    @NotNull
    public List<ObjJVariableName> getInFile(@NotNull String fileName, @NotNull Project project) {
        return getInFile(fileName, null, project);
    }

    @NotNull
    public List<ObjJVariableName> getInFile(@NotNull String fileName, @Nullable GlobalSearchScope searchScope, @NotNull Project project) {
        return get(fileName+"-ALL", project, searchScope);
    }

    @NotNull
    public List<ObjJVariableName> getInRange(@NotNull String fileName, @NotNull TextRange textRange, @NotNull Project project) {
        return getInRange(fileName, textRange, null, project);
    }

    @NotNull
    public List<ObjJVariableName> getInRange(@NotNull String fileName, @NotNull TextRange textRange, @Nullable GlobalSearchScope searchScope, @NotNull Project project) {
        return getInRange(fileName, textRange.getStartOffset(), textRange.getEndOffset(), searchScope, project);
    }

    @NotNull
    public List<ObjJVariableName> getInRange(@NotNull String fileName, int elementStartOffset, int elementEndOffset, @NotNull Project project) {
        return getInRange(fileName, elementStartOffset, elementEndOffset, null, project);
    }

    @NotNull
    public List<ObjJVariableName> getInRange(@NotNull String fileName, int elementStartOffset, int elementEndOffset, @Nullable
            GlobalSearchScope scope, @NotNull Project project) {
        String queryKey = getIndexKey(fileName, elementStartOffset, elementEndOffset);
        return get(queryKey, project, scope);
        //return getAllForKeysFlat(getKeysInRange(fileName, elementStartOffset, elementEndOffset, project), project, scope);
    }

    @NotNull
    private List<String> getKeysInRange(@NotNull String fileName, int blockStart, int blockEnd, @NotNull Project project) {
        return getKeysInRange(fileName, blockStart, blockEnd, null, project);
    }

    @NotNull
    private List<String> getKeysInRange(@NotNull String fileName, int blockStart, int blockEnd, @Nullable GlobalSearchScope searchScope, @NotNull Project project) {
        List<String> keys = new ArrayList<>();
        String queryKey = getIndexKey(fileName, "(\\d+)", "(\\d+)");

        final Pattern pattern = Pattern.compile(queryKey);
        Matcher matcher;
        for (String key : getKeysByPattern(queryKey, project)) {
            matcher = pattern.matcher(key);
            if (matcher.groupCount() < 3) {
                continue;
            }
            int startOffset = Integer.parseInt(matcher.group(1));
            if (blockStart < startOffset) {
                continue;
            }
            int endOffset = Integer.parseInt(matcher.group(2));
            if (endOffset <= blockEnd) {
                keys.add(key);
            }
        }
        return keys;
    }

    @NotNull
    public static String getIndexKey(@NotNull
                                              ObjJBlock block) {
        final String fileName = ObjJFileUtil.getContainingFileName(block.getContainingFile());
        final TextRange textRange = block.getTextRange();
        final int startOffset = textRange.getStartOffset();
        final int endOffset = textRange.getEndOffset();
        return getIndexKey(fileName, startOffset,endOffset);
    }

    public static String getIndexKey(String fileName, Pair<Integer,Integer> blockRange) {
        return getIndexKey(fileName, blockRange.getFirst(), blockRange.getSecond());
    }

    public static String getIndexKey(String fileName, int startOffset, int endOffset) {
        return getIndexKey(fileName, startOffset+"", endOffset+"");
    }

    private static String getIndexKey(String fileName, String startOffset, String endOffset) {
        return String.format(KEY_FORMAT, fileName, startOffset, endOffset);
    }
}
