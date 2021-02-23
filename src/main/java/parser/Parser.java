package parser;

import norswap.autumn.Autumn;
import norswap.autumn.Grammar;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.LineMapString;

public final class Parser extends Grammar {
    { ws = lazy(() -> seq(usual_whitespace, this.comment.opt())); }

    public rule PLUS = word("+");
    public rule MINUS = word("-");
    public rule MUL = word("*");
    public rule DIV = word("/");
    public rule MOD = word("%");
    public rule LINECOMMENT = word("#");
    public rule BLOCKCOMMENTSTART = word("/*");
    public rule BLOCKCOMMENTEND = word("*/");


    public rule lineComment = lazy(() ->
            seq(LINECOMMENT, seq(alphanum.at_least(0).word()),"\n"));

    public rule blockComment = lazy(() ->
            seq(BLOCKCOMMENTSTART, this.operation, BLOCKCOMMENTEND));

    public rule comment = choice(lineComment, blockComment).word();

    public rule variable = seq(opt(MINUS), alphanum.at_least(1));
//            .push($ -> $.str()); // TODO : edge cases

    public rule integer = seq(opt(MINUS), choice('0', digit.at_least(1)));
//            .push($ -> Integer.parseInt($.str()));

    public rule bool = choice("true", "false");

    public rule value = choice(
            integer, bool, variable).word();

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

    public rule operation = choice(comment, addition, multiplication, value);

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
