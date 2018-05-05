package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import org.cappuccino_project.ide.intellij.plugin.exceptions.IndexNotReadyRuntimeException;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJMethodHeaderDeclaration;
import org.cappuccino_project.ide.intellij.plugin.utils.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ObjJMethodHeaderDeclarationsIndexBase<MethodHeaderT extends ObjJMethodHeaderDeclaration> extends ObjJStringStubIndexBase<MethodHeaderT> {

    private static final Logger LOGGER = Logger.getLogger(ObjJMethodHeaderDeclarationsIndexBase.class.getCanonicalName());
    private static final int VERSION = 0;
    private static final Pattern PARTS_PATTERN = Pattern.compile("(_?[a-z]*)?(_?[A-Z0-9][a-z]*)*");

    @Override
    public int getVersion() {
        return super.getVersion() + VERSION;
    }


    @NotNull
    public Map<String, List<MethodHeaderT>> getByPatternFuzzy(@Nullable
                                                                  String patternString,
                                                              @Nullable String part,
                                                              @NotNull
                                                                  Project project) throws IndexNotReadyRuntimeException {
        return getByPatternFuzzy(patternString, part, project, null);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public  Map<String, List<MethodHeaderT>> getByPatternFuzzy(@Nullable String patternString,
                                                               @Nullable String part,
                                                               @NotNull Project project,
                                                               @Nullable GlobalSearchScope globalSearchScope) throws IndexNotReadyRuntimeException {
        if (patternString == null) {
            return emptyList;
        }
        return getAllForKeys(getKeysByPatternFuzzy(patternString, part, project, globalSearchScope), project, globalSearchScope);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public  List<String> getKeysByPatternFuzzy(@Nullable String patternString, @Nullable String selectorPart, @NotNull Project project, @Nullable GlobalSearchScope globalSearchScope) throws IndexNotReadyRuntimeException {
        if (patternString == null) {
            return Collections.emptyList();
        }
        List<String> parts = getParts(selectorPart);
        List<String> matchingKeys = new ArrayList<>();
        List<String> nonMatchingKeys = new ArrayList<>();
        Pattern pattern = Pattern.compile(patternString);
        Matcher matches;
        LOGGER.log(Level.INFO, "Searching for keys with pattern: <"+patternString+">");
        for (String key : getAllKeys(project)) {

            //Skip already checked key
            if (matchingKeys.contains(key) || nonMatchingKeys.contains(key)) {
                continue;
            }
            //Match current key
            matches = pattern.matcher(key);
            if (!matches.matches()) {
                nonMatchingKeys.add(key);
                continue;
            }
            if (matches.groupCount() < 2) {
                LOGGER.log(Level.INFO, "Match returned with only <"+matches.groupCount()+"> match group instead of the minimum of <2>");
            }
            if (parts.isEmpty()) {
                matchingKeys.add(key);
                continue;
            }
            String keyPart = matches.group(1).toLowerCase();
            boolean isMatch = true;
            for (String part : parts) {
                if (!keyPart.contains(part)) {
                    LOGGER.log(Level.INFO, "Key <"+keyPart+"> does not contain part: <"+part+">");
                    nonMatchingKeys.add(key);
                    isMatch = false;
                    break;
                }
            }
            if (isMatch) {
                matchingKeys.add(key);
            }
        }
        LOGGER.log(Level.INFO, "Found <"+matchingKeys.size()+"> matching keys.");
        return matchingKeys;
    }

    @NotNull
    private static List<String> getParts(@Nullable String parts) {
        if (parts == null || parts.isEmpty()) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        Matcher matcher = PARTS_PATTERN.matcher(parts);
        if (!matcher.matches()) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        int numMatches = matcher.groupCount();
        ArrayList<String> out = new ArrayList<>();
        for (int i=1;i<numMatches;i++) {
            out.add(matcher.group(i).toLowerCase());
        }
        return out;
    }

}
