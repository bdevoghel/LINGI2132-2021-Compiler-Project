/*
 * In part inspired by https://github.com/norswap/autumn/blob/master/examples/norswap/lang/java/JavaGrammar.java, especially for the lexical parsing
 */

package parser;

import AST.*;
import norswap.autumn.Autumn;
import norswap.autumn.Grammar;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.autumn.actions.StackPush;
import norswap.autumn.positions.LineMapString;

import static AST.BinaryOperator.*;
import static AST.UnaryOperator.*;

public final class KneghelParser extends Grammar {

    // LEXICAL

    // Whitespace

    public rule LINECOMMENT = str("//");
    public rule BLOCKCOMMENTSTART = str("/*");
    public rule BLOCKCOMMENTEND = str("*/");

    public rule notLine = seq(str("\n").not(), any);
    public rule lineComment = seq(LINECOMMENT, notLine.at_least(0), str("\n").opt());

    public rule notCommentEnd = seq(BLOCKCOMMENTEND.not(), any);
    public rule blockComment = seq(BLOCKCOMMENTSTART, notCommentEnd.at_least(0), BLOCKCOMMENTEND);

    public rule spaceChar = cpred(Character::isWhitespace);
    public rule whitespace = choice(spaceChar, lineComment, blockComment).at_least(0);

    { ws = whitespace; }

    public rule id_start = choice(alpha, "_");
    { id_part = choice(alphanum, "_"); }

    // Keywords and Operators
    public rule EQ = word("=");
    public rule EQEQ = word("==");
    public rule NEQ = word("!=");
    public rule LT = word("<");
    public rule GT = word(">");
    public rule LTEQ = word("<=");
    public rule GTEQ = word(">=");
    public rule PLUS = word("+");
    public rule MINUS = word("-");
    public rule MUL = word("*");
    public rule DIV = word("/");
    public rule MOD = word("%");
    public rule AMP = word("&");
    public rule BAR = word("|");
    public rule AMPAMP = word("&&");
    public rule BARBAR = word("||");
    public rule EXCLAM = word("!");
    public rule QUOTE = word("\"");
    public rule BACKSLASH = word("\\");

    public rule OPENBRACE = word("{");
    public rule OPENPARENT = word("(");
    public rule CLOSEBRACE = word("}");
    public rule CLOSEPARENT = word(")");

    public rule COMMA = word(",");

    public rule _true = reserved("true")    .as_val(true);
    public rule _false = reserved("false")  .as_val(false);
    public rule _null = reserved("null")    .as_val(null);

    public rule _if = reserved("if");
    public rule _else = reserved("else");
    public rule _while = reserved("while");
    public rule _fun = reserved("fun");

    // Variable name
    public rule identifier = identifier((seq(id_start, id_part.at_least(0))))
            .push($ -> new IdentifierNode($.str()));


    // Literal
//    public rule neg = choice(seq(MINUS, choice(integer, identifier).ahead()), seq(NOT, choice(bool, identifier).ahead()))
//            .push($ -> new NegationNode($.str()));
//    public rule minus =

    public rule integer = seq(opt(MINUS), choice('0', digit.at_least(1)))
            .push($ -> new IntegerNode(Integer.parseInt($.str().replaceAll("\\s+",""))));

    public rule bool = choice(_true, _false)
            .push($ -> new BooleanNode(Boolean.parseBoolean($.str())));

    public rule notStringEnd = seq(QUOTE.not(), any);
    public rule string = seq("\"", notStringEnd.at_least(0), "\"") // TODO to modify (see norswap.java)
            .push($ -> {
                String s = $.str();
                return new StringNode(s.substring(1, s.length()-1)); // slice String without quotes
            });

    public rule value = choice(integer, bool, identifier, string, _null).word();


    // EXPRESSIONS

    StackPush pushBinaryExpression = $ -> new BinaryExpressionNode($.$0(), $.$1(), $.$2());
    StackPush pushUnaryExpression = $ -> new UnaryExpressionNode($.$0(), $.$1());

    public rule prefixOp = choice(
            MINUS   .as_val(NEG),
            EXCLAM  .as_val(NOT));
    public rule multOp = choice(
            MUL     .as_val(MULTIPLY),
            DIV     .as_val(DIVIDE),
            MOD     .as_val(MODULO));
    public rule addOp = choice(
            PLUS    .as_val(ADD),
            MINUS   .as_val(SUBTRACT));
    public rule eqOp = choice(
            LTEQ    .as_val(LESS_OR_EQUAL),
            GTEQ    .as_val(GREATER_OR_EQUAL),
            LT      .as_val(LESS_THAN),
            GT      .as_val(GREATER_THAN),
            NEQ     .as_val(NOT_EQUAL),
            EQEQ    .as_val(EQUAL));

    public rule prefixExpression = right_expression()
            .prefix(prefixOp, pushUnaryExpression)
            .right(value);

    public rule multiplicationExpression = left_expression()
            .operand(prefixExpression)
            .infix(multOp, pushBinaryExpression);

    public rule additionExpression = left_expression()
            .operand(multiplicationExpression)
            .infix(addOp, pushBinaryExpression);

    public rule eqExpression = left_expression()
            .operand(additionExpression)
            .infix(eqOp, pushBinaryExpression);

    public rule logicAndExpression = left_expression()
            .operand(eqExpression)
            .infix(AMPAMP.as_val(AND), pushBinaryExpression);

    public rule logicOrExpression = left_expression()
            .operand(logicAndExpression)
            .infix(BARBAR.as_val(OR), pushBinaryExpression);

    public rule logicExpression = choice(logicOrExpression, bool);

    public rule expression = choice(logicExpression); // TODO simplify ??

    public rule variableDefinition = seq(identifier, EQ, expression)
            .push($ -> new AssignmentNode($.$0(), $.$1()));

    public rule block = lazy(() -> seq(OPENBRACE, this.statement.at_least(0), CLOSEBRACE));

    public rule statement = lazy(() -> choice(
            variableDefinition,
            block,
            this.ifStatement,
            this.whileStatement,
            this.functionStatement)); // TODO to complete ?

    public rule ifStatement = seq(_if, logicExpression, statement, seq(_else, statement).or_push_null())
            .push($ -> new IfStatementNode($.$0(), $.$1(), $.$2()));

    public rule whileStatement = seq(_while, logicExpression, statement)
            .push($ -> new WhileStatementNode($.$0(), $.$1()));

    public rule functionStatement = seq(_fun,
            OPENPARENT, identifier.opt(), seq(COMMA, identifier).at_least(0), CLOSEPARENT,
            OPENBRACE, statement, CLOSEBRACE) // TODO
            .push($ -> null);

    public rule root = seq(ws, choice(statement, expression));

    @Override public rule root() {
        return root;
    }

    public ParseResult parse (String input) {
        ParseResult result = Autumn.parse(root, input, ParseOptions.get());
        if (result.fullMatch) {
            System.out.println(result.toString());
        } else {
            // debugging
            System.out.println(result.toString(new LineMapString(input), false, "<input>"));
            // for users
            System.out.println(result.userErrorString(new LineMapString(input), "<input>"));
        }
        return result;
    }
}
