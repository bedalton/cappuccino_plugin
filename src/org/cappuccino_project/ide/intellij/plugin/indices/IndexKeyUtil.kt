package org.cappuccino_project.ide.intellij.plugin.indices

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubIndexExtension
import com.intellij.psi.stubs.StubIndexKey

object IndexKeyUtil {
    fun <K, Psi : PsiElement> createIndexKey(indexClass: Class<out StubIndexExtension<K, Psi>>): StubIndexKey<K, Psi> {
        return StubIndexKey.createIndexKey(indexClass.canonicalName)
    }
}
