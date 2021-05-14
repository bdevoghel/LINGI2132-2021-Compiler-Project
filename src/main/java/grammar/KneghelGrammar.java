package grammar;

import norswap.autumn.Grammar;
import ast.*;

import static ast.UnaryOperator.*;
import static ast.BinaryOperator.*;


@SuppressWarnings("Convert2MethodRef")
public class KneghelGrammar extends Grammar
{
    // ==== LEXICAL ===========================================================

    public rule line_comment =
            seq("//", seq(not("\n"), any).at_least(0));

    public rule multiline_comment =
            seq("/*", seq(not("*/"), any).at_least(0), "*/");

    public rule ws_item = choice(
            set(" \t\n\r;"),
            line_comment,
            multiline_comment);

    {
        ws = ws_item.at_least(0);
        id_part = choice(alphanum, '_');
    }

    public rule STAR            = word("*");
    public rule SLASH           = word("/");
    public rule PERCENT         = word("%");
    public rule PLUS            = word("+");
    public rule MINUS           = word("-");
    public rule LBRACE          = word("{");
    public rule RBRACE          = word("}");
    public rule LPAREN          = word("(");
    public rule RPAREN          = word(")");
    public rule LSQUARE         = word("[");
    public rule RSQUARE         = word("]");
//    public rule COLON           = word(":");
    public rule EQUALS_EQUALS   = word("==");
    public rule EQUALS          = word("=");
    public rule BANG_EQUAL      = word("!=");
    public rule LANGLE_EQUAL    = word("<=");
    public rule RANGLE_EQUAL    = word(">=");
    public rule LANGLE          = word("<");
    public rule RANGLE          = word(">");
    public rule AMP_AMP         = word("&&");
    public rule BAR_BAR         = word("||");
    public rule BANG            = word("!");
//    public rule DOT             = word(".");
//    public rule DOLLAR          = word("$");
    public rule COMMA           = word(",");

//    public rule _var            = reserved("var");
    public rule _class          = reserved("class");
    public rule _fun            = reserved("fun");
//    public rule _struct         = reserved("struct");
    public rule _if             = reserved("if");
    public rule _else           = reserved("else");
    public rule _while          = reserved("while");
    public rule _return         = reserved("return");

    public rule _true           = reserved("true")        .push($ -> new BoolLiteralNode($.span(), true));
    public rule _false          = reserved("false")       .push($ -> new BoolLiteralNode($.span(), false));
    public rule _null           = reserved("null")        .push($ -> new NullLiteralNode($.span()));


    public rule number =
            seq(MINUS.opt(), choice('0', digit.at_least(1)));

    public rule integer =
            number
                    .push($ -> new IntLiteralNode($.span(), Long.parseLong($.str().replaceAll("\\s", ""))))
                    .word();

    public rule fractional =
            seq('.', digit.at_least(0));

    public rule exponent =
            seq(set("eE"), set("+-").opt(), choice('0', digit.at_least(1)));

    public rule floating =
            seq(number, fractional, exponent.opt())
                    .push($ -> new FloatLiteralNode($.span(), Double.parseDouble($.str().replaceAll("\\s", ""))))
                    .word();

    public rule reserved_lit =
            choice(_true, _false, _null)
                    .word();

    public rule string_char = choice(
            seq(set('"', '\\').not(), any),
            seq('\\', set("\\nrt")));

    public rule string_content =
            string_char.at_least(0)
                    .push($ -> $.str());

    public rule string =
            seq('"', string_content, '"')
                    .push($ -> new StringLiteralNode($.span(), $.$[0]))
                    .word();

    public rule identifier =
            identifier(seq(choice(alpha, '_'), id_part.at_least(0)))
                    .push($ -> $.str());

    // ==== SYNTACTIC =========================================================

    public rule reference =
            identifier
                    .push($ -> new ReferenceNode($.span(), $.$[0]));

//    public rule constructor =
//            seq(DOLLAR, reference)
//                    .push($ -> new ConstructorNode($.span(), $.$[0]));
//
//    public rule simple_type =
//            identifier
//                    .push($ -> new SimpleTypeNode($.span(), $.$[0]));

    public rule paren_expression = lazy(() ->
            seq(LPAREN, this.expression, RPAREN)
                    .push($ -> new ParenthesizedNode($.span(), $.$[0])));

    public rule expressions = lazy(() ->
            this.expression.sep(0, COMMA)
                    .as_list(ExpressionNode.class));

    public rule array =
            seq(LSQUARE, expressions, RSQUARE)
                    .push($ -> new ArrayLiteralNode($.span(), $.$[0]));

    public rule basic_expression = choice(
//            constructor,
            reference,
            floating,
            integer,
            reserved_lit,
            string,
            paren_expression,
            array);

    public rule function_args =
            seq(LPAREN, expressions, RPAREN);

    public rule suffix_expression = left_expression()
            .left(basic_expression)
//            .suffix(seq(DOT, identifier),
//                    $ -> new FieldAccessNode($.span(), $.$[0], $.$[1]))
            .suffix(seq(LSQUARE, lazy(() -> this.expression), RSQUARE),
                    $ -> new ArrayAccessNode($.span(), $.$[0], $.$[1]))
            .suffix(function_args,
                    $ -> new FunCallNode($.span(), $.$[0], $.$[1]));

    public rule prefix_op = choice(
            MINUS   .as_val(NEG),
            BANG    .as_val(NOT));

    public rule prefix_expression = right_expression()
            .operand(suffix_expression)
            .prefix(prefix_op,
                    $ -> new UnaryExpressionNode($.span(), $.$[0], $.$[1]));

    public rule mult_op = choice(
            STAR         .as_val(BinaryOperator.MULTIPLY),
            SLASH        .as_val(BinaryOperator.DIVIDE),
            PERCENT      .as_val(BinaryOperator.REMAINDER));

    public rule add_op = choice(
            PLUS         .as_val(BinaryOperator.ADD),
            MINUS        .as_val(BinaryOperator.SUBTRACT));

    public rule cmp_op = choice(
            EQUALS_EQUALS.as_val(BinaryOperator.EQUALITY),
            BANG_EQUAL   .as_val(BinaryOperator.NOT_EQUALS),
            LANGLE_EQUAL .as_val(BinaryOperator.LOWER_EQUAL),
            RANGLE_EQUAL .as_val(BinaryOperator.GREATER_EQUAL),
            LANGLE       .as_val(BinaryOperator.LOWER),
            RANGLE       .as_val(BinaryOperator.GREATER));

    public rule mult_expr = left_expression()
            .operand(prefix_expression)
            .infix(mult_op,
                    $ -> new BinaryExpressionNode($.span(), $.$[0], $.$[1], $.$[2]));

    public rule add_expr = left_expression()
            .operand(mult_expr)
            .infix(add_op,
                    $ -> new BinaryExpressionNode($.span(), $.$[0], $.$[1], $.$[2]));

    public rule order_expr = left_expression()
            .operand(add_expr)
            .infix(cmp_op,
                    $ -> new BinaryExpressionNode($.span(), $.$[0], $.$[1], $.$[2]));

    public rule and_expression = left_expression()
            .operand(order_expr)
            .infix(AMP_AMP.as_val(BinaryOperator.AND),
                    $ -> new BinaryExpressionNode($.span(), $.$[0], $.$[1], $.$[2]));

    public rule or_expression = left_expression()
            .operand(and_expression)
            .infix(BAR_BAR.as_val(BinaryOperator.OR),
                    $ -> new BinaryExpressionNode($.span(), $.$[0], $.$[1], $.$[2]));

    public rule expression =
            choice(or_expression);

    public rule expression_stmt =
            expression
                    .filter($ -> {
                        if ($.$[0] instanceof FunCallNode) {
                            $.push(new ExpressionStatementNode($.span(), $.$[0]));
                            return true;
                        } else {
                            return false;
                        }
                    });

//    public rule array_type = left_expression()
//            .left(simple_type)
//            .suffix(seq(LSQUARE, RSQUARE),
//                    $ -> new ArrayTypeNode($.span(), $.$[0]));
//
//    public rule type =
//            seq(array_type);

    public rule assignment_stmt = left_expression()
            .left(seq(reference, array.or_push_null()))
            .infix(EQUALS)
            .right(expression)
            .requireOperator()
            .push($ -> new AssignmentNode($.span(),
                    $.$[1]==null ? $.$[0] : new ArrayAccessNode($.span(), $.$[0], $.$[1]),
                    $.$[2]));

    public rule statement = lazy(() -> choice(
            this.block,
//            this.var_decl,
            this.fun_decl,
//            this.struct_decl,
            this.if_stmt,
            this.while_stmt,
            this.return_stmt,
            this.assignment_stmt,
            this.expression_stmt));

    public rule statements =
            statement.at_least(0)
                    .as_list(StatementNode.class);

    public rule block =
            seq(LBRACE, statements, RBRACE)
                    .push($ -> new BlockNode($.span(), $.$[0]));

//    public rule var_decl =
//            seq(_var, identifier, COLON, type, EQUALS, expression)
//                    .push($ -> new VarDeclarationNode($.span(), $.$[0], $.$[1], $.$[2]));

    public rule parameter =
            seq(identifier)//, COLON, type)
                    .push($ -> new ParameterNode($.span(), $.$[0]/*, $.$[1]*/));

    public rule parameters =
            parameter.sep(0, COMMA)
                    .as_list(ParameterNode.class);

//    public rule maybe_return_type =
//            seq(COLON, type).or_push_null();

    public rule fun_decl =
            seq(_fun, identifier, LPAREN, parameters, RPAREN, block)
                    .push($ -> new FunDeclarationNode($.span(), $.$[0], $.$[1], $.$[2]));

//    public rule field_decl =
//            seq(_var, identifier, COLON, type)
//                    .push($ -> new FieldDeclarationNode($.span(), $.$[0], $.$[1]));
//
//    public rule struct_body =
//            seq(LBRACE, field_decl.at_least(0).as_list(DeclarationNode.class), RBRACE);
//
//    public rule struct_decl =
//            seq(_struct, identifier, struct_body)
//                    .push($ -> new StructDeclarationNode($.span(), $.$[0], $.$[1]));

    public rule if_stmt = lazy(() ->
            seq(_if, expression, block, seq(_else, choice(block, this.if_stmt)).or_push_null())
                    .push($ -> new IfNode($.span(), $.$[0], $.$[1], $.$[2])));

    public rule while_stmt =
            seq(_while, expression, block)
                    .push($ -> new WhileNode($.span(), $.$[0], $.$[1]));

    public rule return_stmt =
            seq(_return, expression.or_push_null())
                    .push($ -> new ReturnNode($.span(), $.$[0]));

    public rule klass =
            seq(_class, identifier, LBRACE, fun_decl.at_least(0).as_list(FunDeclarationNode.class), RBRACE)
                    .push($ -> new ClassNode($.span(), $.$[0], $.$[1]));

    public rule root =
            seq(ws, klass);

    @Override public rule root () {
        return root;
    }
}
