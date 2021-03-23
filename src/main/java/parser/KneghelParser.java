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

import java.util.Arrays;

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
    public rule OPENBRACKET = word("[");
    public rule CLOSEBRACE = word("}");
    public rule CLOSEPARENT = word(")");
    public rule CLOSEBRACKET = word("]");

    public rule COMMA = word(",");

    public rule _true = reserved("true")    .as_val(true);
    public rule _false = reserved("false")  .as_val(false);
    public rule _null = reserved("null")    .as_val(null);


    public rule _if = reserved("if");
    public rule _else = reserved("else");
    public rule _while = reserved("while");
    public rule _fun = reserved("fun");
    public rule _class = reserved("class");
    public rule _return = reserved("return");
    public rule _main = reserved("main")    .as_val("main");
//    public rule _args = reserved("args");//    .as_val("args");
//    public rule _print = reserved("print");//  .as_val(word("print"));
//    public rule _int = reserved("int");//      .as_val(word("int"));

    // Variable name
    public rule identifier = identifier((seq(id_start, id_part.at_least(0))))
            .push($ -> new IdentifierNode($.span(), $.str()));


    // Literal

    public rule integer = seq(MINUS.opt(), choice('0', digit.at_least(1)))
            .push($ -> new IntegerNode($.span(), Integer.parseInt($.str().replaceAll("\\s+",""))));

    public rule fractional = seq('.', digit.at_least(1));

    public rule exponent = seq(set("eE"), set("+-").opt(), choice('0', digit.at_least(1)));

    public rule doub = seq(MINUS.opt(), choice('0', digit.at_least(1)), fractional.opt(), exponent.opt())
            .push($ -> new DoubleNode($.span(), Double.parseDouble($.str().replaceAll("\\s+",""))));

    public rule number = choice(integer, doub);

    public rule bool = choice(_true, _false)
            .push($ -> new BooleanNode($.span(), Boolean.parseBoolean($.str())));

    public rule notStringEnd = seq(QUOTE.not(), any); // TODO add escape literals
    public rule string = seq("\"", notStringEnd.at_least(0), "\"")
            .push($ -> {
                String s = $.str();
                return new StringNode($.span(), s.substring(1, s.length()-1)); // slice String without quotes
            });

    public rule value = lazy(() -> choice(
            this.functionCallExpression,
            this.arrayMapAccessExpression,
            number, bool, identifier, string, _null
            ).word());


    // EXPRESSIONS & STATEMENTS

    // Simple expressions
    StackPush pushBinaryExpression = $ -> new BinaryExpressionNode($.span(), $.$0(), $.$1(), $.$2());
    StackPush pushUnaryExpression = $ -> new UnaryExpressionNode($.span(), $.$0(), $.$1());

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

    public rule logicExpression = logicOrExpression;

    public rule arrayMapAccessExpression = lazy(() -> seq(identifier, OPENBRACKET, this.expression, CLOSEBRACKET))
            .push($ -> new ArrayMapAccessNode($.span(), $.$0(), $.$1()));

    public rule expression = lazy(() -> choice(this.functionCallExpression, arrayMapAccessExpression, logicExpression));

    public rule returnStatement = seq(_return, expression)
            .push($ -> new ReturnStatementNode($.span(), $.$0()));

    // Simple statements
    public rule variableDefinition = seq(choice(arrayMapAccessExpression, identifier), EQ, expression)
            .push($ -> new AssignmentNode($.span(), $.$0(), $.$1()));

    public rule block = lazy(() -> seq(OPENBRACE, this.statement.at_least(0), CLOSEBRACE));

    public rule statement = lazy(() -> choice(
            block,
            variableDefinition,
            this.ifStatement,
            this.whileStatement,
            this.functionStatement));

    public rule statementBody = seq(statement.at_least(0), returnStatement.opt())
            .push($ -> $.$list());

    public rule ifStatement = seq(_if, logicExpression, OPENBRACE, statementBody, CLOSEBRACE, seq(_else, statementBody).or_push_null())
            .push($ -> new IfStatementNode($.span(), $.$0(), $.$1(), $.$2()));

    public rule whileStatement = seq(_while, logicExpression, OPENBRACE, statementBody, CLOSEBRACE)
            .push($ -> new WhileStatementNode($.span(), $.$0(), $.$1()));

    // Print & Parse
//    public rule printStatement = seq(_print, OPENPARENT, choice(string, identifier), CLOSEPARENT)
//            .push($ -> new PrintStatementNode($.$0()));

//    public rule parseStringToInt = seq(_int, OPENPARENT, choice(string,identifier), CLOSEPARENT)
//            .push($ -> new ParsingNode($.$0()));

    // Function

    public rule functionArguments = seq(OPENPARENT, expression.sep(0, COMMA), CLOSEPARENT)
            .push($ -> new FunctionArgumentsNode($.span(), $.$list()));

    public rule functionHeader = seq(_fun, identifier, functionArguments)
            .push($ -> new FunctionStatementNode($.span(), $.$0(), $.$1()));

    public rule functionMainHeader = seq(_fun, _main,
            OPENPARENT, word("args").as_val(new IdentifierNode(null, "args")), CLOSEPARENT) //TODO null for span
            .push($ -> new FunctionStatementNode(
                    $.span(),
                    new IdentifierNode($.span(), $.$0()),
                    new FunctionArgumentsNode($.span(),Arrays.asList( (IdentifierNode)$.$1() ))));

    public rule functionStatement = seq(choice(functionMainHeader, functionHeader), OPENBRACE, statementBody, CLOSEBRACE)
            .push($ -> ((FunctionStatementNode) $.$0()).setStatement($.$1()));

    public rule functionCallExpression = seq(identifier, functionArguments)
            .push($ -> new FunctionCallNode($.span(),$.$0(), $.$1()));

    // Class
    public rule classBody = seq(OPENBRACE, functionStatement.at_least(0), CLOSEBRACE)
            .push($ -> new ClassStatementNode($.span(),$.$list()));

    public rule classStatement = seq(_class, identifier, classBody)
            .push($ -> ((ClassStatementNode) $.$1()).setIdentifier($.$0()));

    // ROOT
    public rule root = seq(ws, classStatement);

    @Override public rule root() {
        return root;
    }

    /**public ParseResult parse (String input) {
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
    }**/
}
