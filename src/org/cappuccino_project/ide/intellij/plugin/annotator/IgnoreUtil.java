package org.cappuccino_project.ide.intellij.plugin.annotator;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.cappuccino_project.ide.intellij.plugin.psi.ObjJComment;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJCompositeElement;
import org.cappuccino_project.ide.intellij.plugin.psi.interfaces.ObjJHasIgnoreStatements;
import org.cappuccino_project.ide.intellij.plugin.psi.utils.ObjJTreeUtil;

import javax.annotation.Nullable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IgnoreUtil {
    private static final Logger LOGGER = Logger.getLogger(IgnoreUtil.class.getName());
    private static final String IGNORE_KEYWORD = "ignore";
    private static final String IN_KEYWORD = "in";

    static boolean shouldIgnore(ObjJCompositeElement elementToPossiblyIgnore, ElementType ignoreElementType) {
        ObjJComment commentElement = getPrecedingComment(elementToPossiblyIgnore);
        while (commentElement != null) {
            String commentText = commentElement.getText().trim().toLowerCase();
            if (commentText.startsWith("ignore")) {
                commentElement = getPrecedingComment(commentElement);
                continue;
            }
            commentText = commentText.substring(IGNORE_KEYWORD.length());
            String[] parts = commentText.split(IN_KEYWORD);
            for (String kind : parts[0].split(",")) {
                if (kind.trim().equals(ElementType.ALL.type) || kind.trim().equals(ignoreElementType.type)) {
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    @Nullable
    private static ObjJComment getPrecedingComment(PsiElement rootElement) {
        PsiElement prev = ObjJTreeUtil.getPreviousNonEmptySibling(rootElement, true);
        if (prev instanceof ObjJComment) {
            return (ObjJComment)rootElement.getPrevSibling();
        }

        while (rootElement.getParent() != null && !(rootElement.getParent() instanceof ObjJHasIgnoreStatements) && !(rootElement.getParent() instanceof PsiFile)) {
            rootElement = rootElement.getParent();
        }
        prev = ObjJTreeUtil.getPreviousNonEmptySibling(rootElement, true);
        if (!(prev instanceof ObjJComment)) {
            return null;
        }
        LOGGER.log(Level.INFO, "Prev sibling is of comment type.");
        return ((ObjJComment) prev);
    }

    public enum ElementType {
        VAR("undeclaredVar"),
        METHOD("missingMethod"),
        FUNCTION("missingFunction"),
        CALL_TARGET("callTarget"),
        METHOD_SCOPE("methodScope"),
        ALL("all");
        public final String type;
        ElementType(final String type) {
            this.type = type;
        }
    }

    @SuppressWarnings("unused")
    public enum Scope {
        FILE("file"),
        BLOCK("block"),
        EXPR("expr"),
        CLASS("class"),
        METHOD("method");
        public final String scope;
        Scope(final String scope) {
            this.scope = scope;
        }
    }
}
