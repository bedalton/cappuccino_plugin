@file:Suppress("unused")

package cappuccino.ide.intellij.plugin.psi.types

import cappuccino.ide.intellij.plugin.stubs.types.ObjJStubTypes

import cappuccino.ide.intellij.plugin.psi.types.ObjJTypes.*
import com.intellij.psi.tree.TokenSet.create

object ObjJTokenSets {

    val INDENT_CHILDREN = create(ObjJ_BLOCK_ELEMENT, ObjJ_BRACKET_LESS_BLOCK, ObjJ_METHOD_BLOCK,
            ObjJ_STATEMENT_OR_BLOCK , ObjJ_INSTANCE_VARIABLE_LIST)

    val HAS_BRACES = create(ObjJ_BLOCK_ELEMENT, ObjJ_METHOD_BLOCK, ObjJ_INSTANCE_VARIABLE_LIST, ObjJ_SWITCH_STATEMENT)

    val BLOCKS_EXT = create(ObjJ_BLOCK_ELEMENT, ObjJ_BRACKET_LESS_BLOCK, ObjJ_METHOD_BLOCK, ObjJ_PROTOCOL_SCOPED_METHOD_BLOCK,
            ObjJ_STATEMENT_OR_BLOCK, ObjJStubTypes.FILE)


    val CLASS_DECLARATIONS = create(ObjJ_IMPLEMENTATION_DECLARATION, ObjJ_PROTOCOL_DECLARATION)

    val COMMENTS = create(ObjJ_SINGLE_LINE_COMMENT, ObjJ_BLOCK_COMMENT)

    val STRING_LITERALS = create(ObjJ_SINGLE_QUOTE_STRING_LITERAL, ObjJ_DOUBLE_QUOTE_STRING_LITERAL)

    val IMPORT_STATEMENTS = create(ObjJ_IMPORT_FILE, ObjJ_IMPORT_FRAMEWORK)

    val IMPORT_BLOCKS = create(ObjJ_IMPORT_BLOCK, ObjJ_INCLUDE_BLOCK);

    val FUNCTION_DECLARATIONS = create(ObjJ_FUNCTION_DECLARATION, ObjJ_FUNCTION_LITERAL, ObjJ_METHOD_BLOCK)

    val DECLARATIONS = create(
            ObjJ_IMPLEMENTATION_DECLARATION,
            ObjJ_FUNCTION_DECLARATION,
            ObjJ_FUNCTION_LITERAL,
            ObjJ_METHOD_BLOCK,
            ObjJ_INSTANCE_VARIABLE_LIST)

    val STATEMENTS = create(
            ObjJ_RETURN_STATEMENT,
            ObjJ_IF_STATEMENT,
            ObjJ_FOR_STATEMENT,
            ObjJ_THROW_STATEMENT,
            ObjJ_SWITCH_STATEMENT,
            ObjJ_TRY_STATEMENT,
            ObjJ_DEBUGGER_STATEMENT,
            ObjJ_DELETE_STATEMENT,
            ObjJ_FUNCTION_DECLARATION,
            ObjJ_INCLUDE_FILE,
            ObjJ_INCLUDE_FRAMEWORK,
            ObjJ_EXPR,
            ObjJ_BODY_VARIABLE_ASSIGNMENT,
            ObjJ_BLOCK_ELEMENT,
            ObjJ_COMMENT
    )

    val EXPRESSIONS = create(
            ObjJ_RETURN_STATEMENT,
            ObjJ_THROW_STATEMENT,
            ObjJ_DEBUGGER_STATEMENT,
            ObjJ_DELETE_STATEMENT,
            ObjJ_EXPR,
            ObjJ_BODY_VARIABLE_ASSIGNMENT
    )

    val NEW_LINE_AFTER = create(
            ObjJ_IF_STATEMENT,
            ObjJ_FOR_STATEMENT,
            ObjJ_SWITCH_STATEMENT,
            ObjJ_TRY_STATEMENT,
            ObjJ_FUNCTION_DECLARATION,
            ObjJ_IMPORT_BLOCK,
            ObjJ_INCLUDE_BLOCK,
            ObjJ_BLOCK_ELEMENT,
            ObjJ_DO_WHILE_STATEMENT,
            ObjJ_FUNCTION_LITERAL
    )

    val LOGIC_OPERATORS = create(
        ObjJ_AND, ObjJ_OR
    )

    val UNARY_OPERATORS = create(
            ObjJ_PLUS_PLUS, ObjJ_MINUS_MINUS
    )

    val COMPARISON_OPERATORS = create(
            ObjJ_LESS_THAN,
            ObjJ_LESS_THAN_EQUALS,
            ObjJ_GREATER_THAN,
            ObjJ_GREATER_THAN_EQUALS
    )
    val EQUALITY_OPERATORS = create(
            ObjJ_EQUALS,
            ObjJ_IDENTITY_EQUALS,
            ObjJ_NOT_EQUALS,
            ObjJ_IDENTITY_NOT_EQUALS
    )

    var BITWISE_OPERATORS = create(
            ObjJ_BIT_AND,
            ObjJ_BIT_OR,
            ObjJ_BIT_XOR
    )

    val MATH_OPERATORS = create(
            ObjJ_PLUS,
            ObjJ_MINUS,
            ObjJ_BIT_NOT,
            ObjJ_NOT,
            ObjJ_MULTIPLY,
            ObjJ_DIVIDE,
            ObjJ_MODULUS
    )

    val ASSIGNMENT_OPERATORS = create(
            ObjJ_MULTIPLY_ASSIGN,
            ObjJ_DIVIDE_ASSIGN,
            ObjJ_MODULUS_ASSIGN,
            ObjJ_PLUS_ASSIGN,
            ObjJ_MINUS_ASSIGN,
            ObjJ_LEFT_SHIFT_ARITHMATIC_ASSIGN,
            ObjJ_RIGHT_SHIFT_ARITHMATIC_ASSIGN,
            ObjJ_LEFT_SHIFT_LOGICAL_ASSIGN,
            ObjJ_RIGHT_SHIFT_LOGICAL_ASSIGN,
            ObjJ_BIT_AND_ASSIGN,
            ObjJ_BIT_XOR_ASSIGN,
            ObjJ_BIT_OR_ASSIGN,
            ObjJ_EQUALS
    )

    val ALL_OPERATORS = create(

            ObjJ_MULTIPLY_ASSIGN,
            ObjJ_DIVIDE_ASSIGN,
            ObjJ_MODULUS_ASSIGN,
            ObjJ_PLUS_ASSIGN,
            ObjJ_MINUS_ASSIGN,
            ObjJ_LEFT_SHIFT_ARITHMATIC_ASSIGN,
            ObjJ_RIGHT_SHIFT_ARITHMATIC_ASSIGN,
            ObjJ_LEFT_SHIFT_LOGICAL_ASSIGN,
            ObjJ_RIGHT_SHIFT_LOGICAL_ASSIGN,
            ObjJ_BIT_AND_ASSIGN,
            ObjJ_BIT_XOR_ASSIGN,
            ObjJ_BIT_OR_ASSIGN,
            ObjJ_EQUALS,
            ObjJ_PLUS,
            ObjJ_MINUS,
            ObjJ_BIT_NOT,
            ObjJ_NOT,
            ObjJ_MULTIPLY,
            ObjJ_DIVIDE,
            ObjJ_MODULUS,
            ObjJ_BIT_AND,
            ObjJ_BIT_OR,
            ObjJ_BIT_XOR,
            ObjJ_EQUALS,
            ObjJ_IDENTITY_EQUALS,
            ObjJ_NOT_EQUALS,
            ObjJ_IDENTITY_NOT_EQUALS,
            ObjJ_LESS_THAN,
            ObjJ_LESS_THAN_EQUALS,
            ObjJ_GREATER_THAN,
            ObjJ_GREATER_THAN_EQUALS
    )

    val METHOD_HEADER_DECLARATION_SELECTOR = create(
            ObjJ_METHOD_DECLARATION_SELECTOR,
            ObjJ_FIRST_METHOD_DECLARATION_SELECTOR
    )

    val HAS_PARAMS = create(
            ObjJ_METHOD_HEADER,
            ObjJ_METHOD_DECLARATION,
            ObjJ_FUNCTION_LITERAL,
            ObjJ_FUNCTION_DECLARATION,
            ObjJ_PREPROCESSOR_DEFINE_FUNCTION
    )

    val PREFIX_OPERATOR = create(
            ObjJ_PLUS,
            ObjJ_MINUS,
            ObjJ_NOT,
            ObjJ_BIT_NOT
    )
    val SHIFT_OPERATOR_SET = create(
            ObjJ_RIGHT_SHIFT_ARITHMATIC,
            ObjJ_LEFT_SHIFT_ARITHMATIC,
            ObjJ_RIGHT_SHIFT_LOGICAL,
            ObjJ_LEFT_SHIFT_LOGICAL
    )

    val REFERENCE_EXPRESSION_SET = create(
            ObjJ_FUNCTION_NAME,
            ObjJ_METHOD_CALL,
            ObjJ_ID,
            ObjJ_QUALIFIED_REFERENCE
    )

    val CALL_EXPRESSIONS = create(
            ObjJ_METHOD_CALL,
            ObjJ_FUNCTION_CALL
    )

    val WHITE_SPACE = create(
            com.intellij.psi.TokenType.WHITE_SPACE,
            ObjJ_LINE_TERMINATOR
    )

    val PREPROC_KEYWORDS = create(
            ObjJ_PP_DEFINE,
            ObjJ_PP_DEFINED,
            ObjJ_PP_ELSE,
            ObjJ_PP_ELSE_IF,
            ObjJ_PP_END_IF,
            ObjJ_PP_ERROR,
            ObjJ_PP_FRAGMENT,
            ObjJ_PP_IF,
            ObjJ_PP_IF_DEF,
            ObjJ_PP_IF_NDEF,
            ObjJ_PP_INCLUDE,
            ObjJ_PP_PRAGMA,
            ObjJ_PP_UNDEF,
            ObjJ_PP_WARNING
    )

    val VAR_TYPE_KEYWORDS = create(
            ObjJ_VAR_TYPE_BOOL,
            ObjJ_VAR_TYPE_INT,
            ObjJ_VAR_TYPE_SHORT,
            ObjJ_VAR_TYPE_LONG,
            ObjJ_VAR_TYPE_LONG_LONG,
            ObjJ_VAR_TYPE_UNSIGNED,
            ObjJ_VAR_TYPE_SIGNED,
            ObjJ_VAR_TYPE_FLOAT,
            ObjJ_VAR_TYPE_DOUBLE,
            ObjJ_VAR_TYPE_BYTE,
            ObjJ_VAR_TYPE_ID
    )

    val NUMERIC_TYPES = create(
            ObjJ_HEX_INTEGER_LITERAL,
            ObjJ_OCTAL_INTEGER_LITERAL,
            ObjJ_OCTAL_INTEGER_LITERAL2,
            ObjJ_DECIMAL_LITERAL,
            ObjJ_BINARY_INTEGER_LITERAL,
            ObjJ_INTEGER_LITERAL
    )

    val ITERATION_STATEMENT_KEYWORDS = create(
            ObjJ_IF,
            ObjJ_ELSE,
            ObjJ_IN,
            ObjJ_FOR,
            ObjJ_WHILE,
            ObjJ_DO
    )

    val INDENT_FIRST_CHILD = create(
            ObjJ_DO_WHILE_STATEMENT,
            ObjJ_WHILE_STATEMENT,
            ObjJ_IF_STATEMENT,
            ObjJ_FOR_STATEMENT
    )

    val CAN_COMPLETE_AFTER = listOf(
            ObjJ_SEMI_COLON,
            ObjJ_DOT,
            ObjJ_OPEN_BRACE,
            ObjJ_CLOSE_BRACE,
            ObjJ_OPEN_BRACKET,
            ObjJ_COLON,
            ObjJ_OPEN_PAREN,
            ObjJ_CALL_TARGET,
            ObjJ_QUALIFIED_METHOD_CALL_SELECTOR,
            ObjJ_ID,
            ObjJ_NOT,
            ObjJ_RETURN,
            ObjJ_IN,
            ObjJ_AT_CLASS,
            ObjJ_QUESTION_MARK,
            ObjJ_ASSIGN,
            ObjJ_MULTIPLY_ASSIGN,
            ObjJ_DIVIDE_ASSIGN,
            ObjJ_MODULUS_ASSIGN,
            ObjJ_PLUS_ASSIGN,
            ObjJ_MATH_OP,
            ObjJ_MINUS_ASSIGN,
            ObjJ_LEFT_SHIFT_ARITHMATIC_ASSIGN,
            ObjJ_RIGHT_SHIFT_ARITHMATIC_ASSIGN,
            ObjJ_LEFT_SHIFT_LOGICAL_ASSIGN,
            ObjJ_RIGHT_SHIFT_LOGICAL_ASSIGN,
            ObjJ_BIT_AND_ASSIGN,
            ObjJ_BIT_XOR_ASSIGN,
            ObjJ_BIT_OR_ASSIGN,
            ObjJ_EQUALS,
            ObjJ_PLUS,
            ObjJ_MINUS,
            ObjJ_BIT_NOT,
            ObjJ_NOT,
            ObjJ_MULTIPLY,
            ObjJ_DIVIDE,
            ObjJ_MODULUS,
            ObjJ_BIT_AND,
            ObjJ_BIT_OR,
            ObjJ_BIT_XOR,
            ObjJ_EQUALS,
            ObjJ_IDENTITY_EQUALS,
            ObjJ_NOT_EQUALS,
            ObjJ_IDENTITY_NOT_EQUALS,
            ObjJ_LESS_THAN,
            ObjJ_LESS_THAN_EQUALS,
            ObjJ_GREATER_THAN,
            ObjJ_GREATER_THAN_EQUALS,
            ObjJ_COMMA,
            ObjJ_NEW,
            ObjJ_DO_WHILE_STATEMENT,
            ObjJ_IF_STATEMENT,
            ObjJ_WHILE_STATEMENT,
            ObjJ_TRY_STATEMENT,
            ObjJ_SWITCH_STATEMENT,
            ObjJ_CASE_CLAUSE,
            ObjJ_DEFAULT_CLAUSE,
            ObjJ_CASE,
            ObjJ_AT_END
    )
}
