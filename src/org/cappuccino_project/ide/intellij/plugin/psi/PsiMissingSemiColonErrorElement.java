package org.cappuccino_project.ide.intellij.plugin.psi;

import com.intellij.psi.impl.source.tree.PsiErrorElementImpl;

public class PsiMissingSemiColonErrorElement extends PsiErrorElementImpl {
    private static final String ERROR_DESCRIPTION = "Missing semi-colon";
    public PsiMissingSemiColonErrorElement() {
        super(ERROR_DESCRIPTION);
    }
}
