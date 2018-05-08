package org.cappuccino_project.ide.intellij.plugin.psi

import com.intellij.psi.impl.source.tree.PsiErrorElementImpl

class PsiMissingSemiColonErrorElement : PsiErrorElementImpl(ERROR_DESCRIPTION) {
    companion object {
        private val ERROR_DESCRIPTION = "Missing semi-colon"
    }
}
