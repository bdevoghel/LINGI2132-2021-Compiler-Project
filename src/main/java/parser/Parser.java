package parser;

import norswap.autumn.Autumn;
import norswap.autumn.Grammar;
import norswap.autumn.ParseOptions;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.LineMapString;

public final class Parser extends Grammar {
    { ws = usual_whitespace; }

    public rule variable = alphanum.at_least(5)
            .push($ -> $.str()); // TODO : edge cases

    public rule integer = seq(opt('-'), choice('0', digit.at_least(1)))
            .push($ -> Integer.parseInt($.str()));

    public rule value = choice(
            integer, variable).word();

    public rule operation = choice(value);

    public rule root = seq(ws, operation);

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
