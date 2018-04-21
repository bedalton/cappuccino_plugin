package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import org.cappuccino_project.ide.intellij.plugin.contributor.ObjJMethodCallCompletionContributorUtil;
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJMethodPsiUtils;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.StringUtil;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.codehaus.groovy.runtime.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ObjJStringStubIndexBase<ObjJElemT extends ObjJCompositeElement>  extends StringStubIndexExtension<ObjJElemT> {

    private static final Logger LOGGER = Logger.getLogger(ObjJStringStubIndexBase.class.getName());
    protected static final Map emptyList = Collections.emptyMap();
    private static final int VERSION = 3;

    @Override
    public int getVersion() {
        return super.getVersion() + ObjJIndexService.INDEX_VERSION + VERSION;
    }


    @NotNull
    public List<ObjJElemT> get(String className, Project project) throws IndexNotReadyRuntimeException {
        return get(className, project, null);
    }

    @NotNull
    public List<ObjJElemT> get(@NotNull String variableName, @NotNull Project project, @Nullable
            GlobalSearchScope searchScope) throws IndexNotReadyRuntimeException {

        if (DumbService.getInstance(project).isDumb()) {
            throw new IndexNotReadyRuntimeException();
            //return Collections.emptyList();
        }
        //LOGGER.log(Level.INFO, "Index("+getClass().getSimpleName()+")->get("+variableName+")");
        return new ArrayList<>(StubIndex.getElements(getKey(), variableName, project, searchScope, getIndexedElementClass()));
    }


    public Map<String, List<ObjJElemT>> getByPattern(@Nullable String start, @Nullable String tail, @NotNull Project project) throws IndexNotReadyRuntimeException {
        return getByPattern(start, tail, project, null);
    }

    @NotNull
    private Map<String, List<ObjJElemT>> getByPattern(
            @Nullable
                    String start,
            @Nullable
                    String tail,
            @NotNull
                    Project project,
            @Nullable
                    GlobalSearchScope globalSearchScope) throws IndexNotReadyRuntimeException {

        ArrayList<String> keys = new ArrayList<>();
        List<String> notMatchingKeys = new ArrayList<>();
        for (String key : getAllKeys(project)) {
            if (notMatchingKeys.contains(key) || keys.contains(key)) {
                continue;
            }
            if (StringUtil.startsAndEndsWith(key, start, tail)) {
                LOGGER.log(Level.INFO, "Found selector matching <"+start+"//"+tail+">: <"+key+">");
                keys.add(key);
            } else {
                notMatchingKeys.add(key);
            }
        }
        return getAllForKeys(keys, project, globalSearchScope);

    }

    @NotNull
    public  Map<String, List<ObjJElemT>> getByPattern(@Nullable String patternString, @NotNull Project project) throws IndexNotReadyRuntimeException {
        return getByPattern(patternString, project, null);
    }



    @SuppressWarnings("unchecked")
    @NotNull
    public  Map<String, List<ObjJElemT>> getByPattern(@Nullable String patternString, @NotNull Project project, @Nullable GlobalSearchScope globalSearchScope) throws IndexNotReadyRuntimeException {
        if (patternString == null) {
            return emptyList;
        }
        return getAllForKeys(getKeysByPattern(patternString, project, globalSearchScope), project, globalSearchScope);
    }


    @NotNull
    protected Map<String, List<ObjJElemT>> getAllForKeys(@NotNull List<String> keys, @NotNull Project project) {
        return getAllForKeys(keys, project, null);
    }

    @NotNull
    protected Map<String, List<ObjJElemT>> getAllForKeys(@NotNull List<String> keys, @NotNull Project project, @Nullable GlobalSearchScope globalSearchScope) throws IndexNotReadyRuntimeException  {
        HashMap<String, List<ObjJElemT>> out = new HashMap<>();
          for (String key : keys) {
              if (out.containsKey(key)) {
                  out.get(key).addAll(get(key, project, globalSearchScope));
              } else {
                  out.put(key, get(key, project, globalSearchScope));
              }
          }
          return out;
    }

    @NotNull
    public List<ObjJElemT> getStartingWith(@NotNull String pattern, @NotNull Project project) {
        return getByPatternFlat(pattern+"(.*)", project);
    }

    @NotNull
    public List<ObjJElemT> getByPatternFlat(@NotNull String pattern, @NotNull Project project) {
        return getByPatternFlat(pattern, project, null);

    }

    @NotNull
    public List<ObjJElemT> getByPatternFlat(@NotNull String pattern, @NotNull Project project, @Nullable GlobalSearchScope scope) {
        List<String> keys = getKeysByPattern(pattern, project, scope);
        return getAllForKeysFlat(keys, project, scope);

    }

    @NotNull
    protected List<ObjJElemT> getAllForKeysFlat(@NotNull List<String> keys, @NotNull Project project, @Nullable GlobalSearchScope globalSearchScope) throws IndexNotReadyRuntimeException  {
        List<ObjJElemT> out = new ArrayList<>();
        List<String> done = new ArrayList<>();
        for (String key : keys) {
            if (!done.contains(key)) {
                done.add(key);
                out.addAll(get(key, project, globalSearchScope));
            }
        }
        return out;
    }


    @SuppressWarnings("unchecked")
    @NotNull
    public  List<String> getKeysByPattern(@Nullable String patternString, @NotNull Project project) throws IndexNotReadyRuntimeException {
        return getKeysByPattern(patternString, project, null);
    }


    @SuppressWarnings("unchecked")
    @NotNull
    public  List<String> getKeysByPattern(@Nullable String patternString, @NotNull Project project, @Nullable GlobalSearchScope globalSearchScope) throws IndexNotReadyRuntimeException {
        if (patternString == null) {
            return Collections.emptyList();
        }
        List<String> matchingKeys = new ArrayList<>();
        List<String> notMatchingKeys = new ArrayList<>();
        Pattern pattern;
        try {
            pattern = Pattern.compile(patternString);
        } catch (Exception e) {
            pattern = Pattern.compile(Pattern.quote(patternString));
        }
        for (String key : getAllKeys(project)) {
            if (notMatchingKeys.contains(key) || matchingKeys.contains(key)) {
                continue;
            }
            if (pattern.matcher(key).matches()) {
                //LOGGER.log(Level.INFO, "Found Matching key for pattern: <"+patternString+">: <"+key+">");
                matchingKeys.add(key);
            } else {
                //LOGGER.log(Level.INFO, "Key <"+key+"> does not match pattern: <"+patternString+">");
                notMatchingKeys.add(key);
            }
        }
        return matchingKeys;
    }

    @NotNull
    public List<ObjJElemT> getAll (@NotNull Project project) throws IndexNotReadyRuntimeException  {
        return getAll(project, null);
    }

    @NotNull
    public List<ObjJElemT> getAll(@NotNull Project project, @Nullable GlobalSearchScope globalSearchScope) throws IndexNotReadyRuntimeException  {
        List<ObjJElemT> out = new ArrayList<>();

        if (DumbService.getInstance(project).isDumb()) {
            throw new IndexNotReadyRuntimeException();
            //return Collections.emptyList();
        }
        for (String key : getAllKeys(project)) {
            out.addAll(get(key, project, globalSearchScope));
        }
        return out;
    }

    @NotNull
    protected abstract Class<ObjJElemT> getIndexedElementClass();
}
