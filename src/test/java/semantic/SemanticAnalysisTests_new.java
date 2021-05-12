package semantic;

import ast_new.*;
import grammar.KneghelGrammar_new;

import norswap.autumn.AutumnTestFixture;
import norswap.autumn.positions.LineMapString;
import norswap.uranium.Reactor;
import norswap.uranium.UraniumTestFixture;
import norswap.utils.visitors.Walker;
import org.testng.annotations.Test;

import java.util.Arrays;


public class SemanticAnalysisTests_new extends UraniumTestFixture {

    private final KneghelGrammar_new grammar = new KneghelGrammar_new();
    private final AutumnTestFixture autumnFixture = new AutumnTestFixture();

    {
        autumnFixture.rule = grammar.root();
        autumnFixture.runTwice = false;
        autumnFixture.bottomClass = this.getClass();
    }

    private String input;

    @Override
    protected Object parse(String input) {
        this.input = input;
        return autumnFixture.success(input).topValue();
    }

    @Override
    protected String astNodeToString(Object ast) {
        LineMapString map = new LineMapString("<test>", input);
        return ast.toString() + " (" + ((KneghelNode) ast).span.startString(map) + ")";
    }

    // ---------------------------------------------------------------------------------------------

    @Override
    protected void configureSemanticAnalysis(Reactor reactor, Object ast) {
        Walker<KneghelNode> walker = SemanticAnalysis_new.createWalker(reactor);
        walker.walk(((KneghelNode) ast));
    }

    @Test
    public void testClass() {
        autumnFixture.rule = grammar.root;
        successInput("class Hello { fun World() {} }");
    }

    @Test
    public void testFunctions() {
        autumnFixture.rule = grammar.root;
        successInput("class Hello { fun World() {a=1} }");
        successInput("class Hello { fun World(a, b) {return a} }");
        successInput("class Hello { fun World(a, b) {return a} fun main(args) {World(1, 2)} }");
        successInput("class Hello { fun World(a) {return a+1} fun main(args) {a=2 return World(a)} }");
    }

    @Test
    public void testInteger() {
        autumnFixture.rule = grammar.integer;
        successInput("5");
        successInput("5655");
        successInput("-1");
    }

    @Test
    public void testFloating() {
        autumnFixture.rule = grammar.floating;
        successInput("5.");
        successInput("5.0");
        successInput("5.6e10");
        successInput("5.6e-1");
        successInput("-5478.32");
    }

    @Test
    public void testBool() {
        autumnFixture.rule = grammar.reserved_lit;
        successInput("true");
        successInput("false");
        successInput("null");
    }

    @Test
    public void testString() {
        autumnFixture.rule = grammar.string;
        successInput("\"abc\"");
        successInput("\" a b c \"");
    }

    @Test
    public void testBinaryExpression() {
        autumnFixture.rule = grammar.add_expr;
        successInput("1 + 2");
        successInput("1 * 2");
        successInput("1 + 2 + 3 - 4");
        successInput("1 + 2 * 3 / 4 + 5");
        successInput("1 - 2 + 3");
        successInput("-1 + 2");
        successInput("1 / 2 + 3");
        successInput("1 + 2 % 3");
        successInput("1.0 + 2 % 3.0");

        successInput("1 + 2. * 3.4e-5 % 6. / 7");
        successInput("1 / 2");
        successInput("1. / 2");
        successInput("1 / 2.");
        successInput("1. / 2.");

        failureInput("1 + true");
        failureInput("1 * true");
        failureInput("false * true");
        failureInput("false + true");

        failureInput("\"a\" + 1");
        failureInput("\"a\" + true");
        failureInput("\"a\" * 1");
        failureInput("\"a\" * true");

        failureInput("\"a\" + \"a\"");
        failureInput("\"a\" * \"a\"");

        // x has to be in scope
        autumnFixture.rule = grammar.root;
        failureInput("class A { fun b(x) {r= 1 + true} }");

        failureInput("class A { fun b(x) {r= x + true} }");
        failureInput("class A { fun b(x) {r= true * x} }");
        failureInput("class A { fun b(x) {r= true + x} }");
        successInput("class A { fun b(x) {r= \"a\" + x} }");
        failureInput("class A { fun b(x) {r= \"a\" * x} }");

        successInput("class A { fun b(x) {r= 1 + x} }");
        failureInput("class A { fun b(x) {r= 1 + a} }");
    }

    @Test
    public void testUnaryExpression() {
        autumnFixture.rule = grammar.prefix_expression;
        successInput("- 1");
        failureInput("- true");
        successInput("! true");
        failureInput("! 1");
        successInput("- 2.0");
    }

    @Test
    public void testComparison() {
        autumnFixture.rule = grammar.expression;
        successInput("1 > 1");
        successInput("1 >= 1");
        successInput("1 < 1");
        successInput("1 <= 1");
        successInput("1 == 1");
        successInput("1 != 1");
        failureInput("true > 1");
        failureInput("true >= 1");
        failureInput("true < 1");
        failureInput("true <= 1");
        successInput("true == 42");
        successInput("true != 42");
        failureInput("true > false");
        failureInput("true >= false");
        failureInput("true < false");
        failureInput("true <= false");
        successInput("true == false");
        successInput("true != false");
        failureInput("\"a\" > 1");
        failureInput("\"a\" >= 1");
        failureInput("\"a\" < 1");
        failureInput("\"a\" <= 1");
        failureInput("\"a\" == 1");
        failureInput("\"a\" != 1");
        failureInput("null > 1");
        failureInput("null >= 1");
        failureInput("null < 1");
        failureInput("null <= 1");
        failureInput("null == 1");
        failureInput("null != 1");
        successInput("\"a\" > \"b\"");
        successInput("\"a\" >= \"b\"");
        successInput("\"a\" < \"b\"");
        successInput("\"a\" <= \"b\"");
        successInput("\"a\" == \"b\"");
        successInput("\"a\" != \"b\"");
    }

    @Test
    public void testIfStatement() {
        autumnFixture.rule = grammar.root;
        successInput("class Foo { fun bar() {if true {a = 1+2}} }");
        successInput("class Foo { fun bar() {if true { a=2 } else { a=3 }} }");
        successInput("class Foo { fun bar(a) {if a == 1 { a=2 b=2 } else if a == true { a=3 } else if a == \"hello\" { a=\"world\" }} }");
        successInput("class Foo { fun bar(a, b) {if b == 2 { print(a) } }}");
    }

    @Test
    public void testWhileStatement() {
        autumnFixture.rule = grammar.root;
        successInput("class Foo { fun bar(a, b) {while a > 2 { a = a - b } }}");
        failureInput("class Foo { fun bar(b) {while true { a = a + b } return a} }");
        successInput("class Foo { fun bar(b) {a = 0 while true { a = a + b } return a} }");
        successInput("class Foo { fun bar(a, b, c) {while a == b { c = b + 2 - a} }}");
        successInput("class Foo { fun bar() {a = 0 b = 1 while true {  a = b+1}} }");
        failureInput("class Foo { fun bar() {while true { a = true + 1}} }");

    }

    @Test
    public void testClasses() {
        autumnFixture.rule = grammar.root;
        successInput("class Foo { }");
        successInput("class Foo { fun bar() {} }");
        successInput("class Foo { fun bar() {a=1} }");
        successInput("class Foo { fun bar(a) {b = a[0] return b} }");
        successInput("class Foo { fun bar(a) {a[0]=1} }");
        successInput("class Foo { fun bar() {a=1 return a} fun fuzz(a) {return a} }");
        successInput("class Foo { fun bar() {a = 1 + 2 return a} }");
        successInput("class Foo { fun bar() {a = 1 + 2 return a} fun fuzz(a) {return a} fun main(args) {bar() fuzz(args[0])} }");
        failureInput("class Foo { fun bar() {a = 1 + 2 return a} fun fuzz(a) {return a} fun main(args) {bar(b) fuzz(args[0])} }");
    }

    @Test
    public void testFunctionStatement() {
        autumnFixture.rule = grammar.root;
        successInput("class Foo { fun bar() { a = 1 return a } }");
        failureInput("class Foo { fun bar() { a = 1 return a } fun bar() { b = 1 return b } }");
    }

    @Test
    public void testMain() {
        autumnFixture.rule = grammar.root;
        successInput("class Foo { fun main(args) { a = 1 return a } }");
        failureInput("class Foo { fun main(args) { a = 1 return a } fun main(args) { b = 1 return b } }");
    }
}
