package org.cappuccino_project.ide.intellij.plugin.psi.types;

import com.intellij.psi.tree.TokenSet;

import static com.intellij.psi.TokenType.WHITE_SPACE;
import static org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes.*;
import static org.cappuccino_project.ide.intellij.plugin.psi.types.ObjJTypes.ObjJ_GLOBAL_VARIABLE_DECLARATION;

public class TokenSets {
    public static final TokenSet WHITE_SPACES = TokenSet.create(WHITE_SPACE);
    public static final TokenSet COMMENTS = TokenSet.create(ObjJ_SINGLE_LINE_COMMENT, ObjJ_BLOCK_COMMENT);
    public static final TokenSet ONE_LINE_ITEMS = TokenSet.create(
            ObjJ_RETURN_STATEMENT,
            ObjJ_CONTINUE,
            ObjJ_BREAK_STATEMENT,
            ObjJ_DELETE_STATEMENT,
            ObjJ_DEBUGGER_STATEMENT,
            ObjJ_INSTANCE_VARIABLE_DECLARATION,
            ObjJ_IMPORT_FRAMEWORK,
            ObjJ_INCLUDE_FRAMEWORK,
            ObjJ_IMPORT_FILE,
            ObjJ_INCLUDE_FILE,
            ObjJ_CLASS_DEPENDENCY_STATEMENT,
            ObjJ_GLOBAL_VARIABLE_DECLARATION);
}
