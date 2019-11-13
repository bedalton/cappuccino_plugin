package cappuccino.ide.intellij.plugin.comments.parser;

import cappuccino.ide.intellij.plugin.lang.ObjJLanguage;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

public class ObjJDocCommentElementType extends IElementType {
    private final Constructor<? extends PsiElement> psiFactory;

    public ObjJDocCommentElementType(String debugName, @NotNull
            Class<? extends PsiElement> psiClass) {
        super(debugName, ObjJLanguage.getInstance());
        try {
            psiFactory = psiClass.getConstructor(ASTNode.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Must have a constructor with ASTNode");
        }
    }

    public PsiElement createPsi(ASTNode node) {
        assert node.getElementType() == this;

        try {
            return psiFactory.newInstance(node);
        } catch (Exception e) {
            throw new RuntimeException("Error creating psi element for node", e);
        }
    }
}
