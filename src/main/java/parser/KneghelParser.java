/*
 * In part inspired by https://github.com/norswap/autumn/blob/master/examples/norswap/lang/java/JavaGrammar.java
 */

package parser;

import AST.*;
import norswap.autumn.Autumn;
import norswap.autumn.Grammar;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.LineMapString;

public final class KneghelParser extends Grammar {

    // LEXICAL

    // Whitespace

    public rule LINECOMMENT = str("//");
    public rule BLOCKCOMMENTSTART = str("/*");
    public rule BLOCKCOMMENTEND = str("*/");

    public rule spaceChar = cpred(Character::isWhitespace);
    public rule notLine = seq(str("\n").not(), any);
    public rule lineComment = seq(LINECOMMENT, notLine.at_least(0), str("\n").opt());

    public rule notCommentTerm = seq(BLOCKCOMMENTEND.not(), any);
    public rule blockComment = seq(BLOCKCOMMENTSTART, notCommentTerm.at_least(0), BLOCKCOMMENTEND);

    public rule whitespace = choice(spaceChar, lineComment, blockComment).at_least(0);

    { ws = whitespace; }

    // Keywords and Operators
    public rule EQ = word("=");
    public rule PLUS = word("+");
    public rule MINUS = word("-");
    public rule MUL = word("*");
    public rule DIV = word("/");
    public rule MOD = word("%");

    public rule _true = word("true").as_val(true);
    public rule _false = word("false").as_val(false);
    public rule _null = word("null").as_val(null);

    // TODO : replace "word" by "reserved" for keywords ??

    // Numerals

    // Characters and Strings

    // Literal
    public rule variable = seq(opt(MINUS), alphanum.at_least(1)).word() // TODO : edge cases
            .push($ -> $.$list().size() > 1 ? new VariableNode($.$0()) : new VariableNode($.$1(), true));

    public rule integer = seq(opt(MINUS), choice('0', digit.at_least(1)))
            .push($ -> new IntegerNode(Integer.parseInt($.str().replaceAll("\\s+",""))));

    public rule bool = choice(_true, _false);

    public rule value = choice(integer, bool, variable).word();

    // EXPRESSIONS

    public rule multiplication = left_expression()
            .operand(value)
            .infix(MUL)
            .infix(DIV)
            .infix(MOD)
            .requireOperator();

    public rule addition = left_expression()
            .operand(choice(multiplication, value))
            .infix(PLUS)
            .infix(MINUS)
            .requireOperator();

    public rule operation = choice(addition, multiplication, value);

    public rule variableDefinition = seq(variable, EQ, operation);

    public rule root = seq(ws, opt(operation)); // opt operation to handle empty parse

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
