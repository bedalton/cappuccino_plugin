package org.cappuccino_project.ide.intellij.plugin.indices;

import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.StubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public class IndexKeyUtil {
    private IndexKeyUtil(){}
    @NotNull
    public static <K, Psi extends PsiElement> StubIndexKey<K, Psi> createIndexKey(@NotNull Class<? extends StubIndexExtension<K, Psi>> indexClass) {
        return StubIndexKey.createIndexKey(indexClass.getCanonicalName());
    }
}
