package semantic;

import AST.ASTNode;
import norswap.autumn.AutumnTestFixture;
import norswap.autumn.positions.LineMapString;
import norswap.uranium.Reactor;
import norswap.uranium.UraniumTestFixture;
import norswap.utils.visitors.Walker;
import grammar.KneghelGrammar;
import org.testng.annotations.Test;


public class SemanticAnalysisTest extends UraniumTestFixture {

    private final KneghelGrammar grammar = new KneghelGrammar();
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
        return ast.toString() + " (" + ((ASTNode) ast).span.startString(map) + ")";
    }

    // ---------------------------------------------------------------------------------------------

    @Override
    protected void configureSemanticAnalysis(Reactor reactor, Object ast) {
        Walker<ASTNode> walker = SemanticAnalysis.createWalker(reactor);
        walker.walk(((ASTNode) ast));
    }

    @Test
    public void testInteger() {
        autumnFixture.rule = grammar.integer;
        successInput("5");
        successInput("5655");
        successInput("-1");
    }

    @Test
    public void testDouble() {
        autumnFixture.rule = grammar.doub;
        successInput("5.");
        successInput("5.0");
        successInput("5.6e10");
        successInput("5.6e-1");
        successInput("-5478.32");
    }

    @Test
    public void testBool() {
        autumnFixture.rule = grammar.bool;
        successInput("true");
        successInput("false");
    }

    @Test
    public void testString() {
        autumnFixture.rule = grammar.string;
        successInput("\"abc\"");
        successInput("\" a b c \"");
    }

    @Test
    public void testBinaryExpression() {
        autumnFixture.rule = grammar.additionExpression;
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
        failureInput("class A { fun b(x) {r= \"a\" + x} }");
        failureInput("class A { fun b(x) {r= \"a\" * x} }");
//        successInput("\"a\" + 1"); // TODO to allow see TODO in arithmetic
//        failureInput("1 + a"); //TODO JE NE SAIS PAS COMMENT FAIRE DES FAILURES
    }

    @Test
    public void testUnaryExpression() {
        autumnFixture.rule = grammar.prefixExpression;
        successInput("-1");
        successInput("!true");
        successInput("- 2.0");
    }


    @Test
    public void testIfStatement() {
        autumnFixture.rule = grammar.root;
        successInput("class Foo { fun bar() {if true {a = 1+2}} }");
        successInput("class Foo { fun bar() {if true { a=2 } else { a=3 }} }");
        successInput("class Foo { fun bar(a) {if a == 1 { a=2 b=2 } else if a == true { a=3 } else if a == \"hello\" { a=\"world\" }} }");
        successInput("class Foo { fun bar(a, b) {if b == 2 { _ = print(a) } }}");
    }

    @Test
    public void testWhileStatement() {
        autumnFixture.rule = grammar.root;
        successInput("class Foo { fun bar(a, b) {while a > 2 { a = a - b } }}");
        successInput("class Foo { fun bar(b) {while true { a = b + 1 } return a} }");
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
        successInput("class Foo { fun bar(a) {a[0]=1} }");
        successInput("class Foo { fun bar() {a=1 return a} fun fuzz(a) {return a} }");
        successInput("class Foo { fun bar() {a = 1 + 2 return a} }");
        successInput("class Foo { fun bar() {a = 1 + 2 return a} fun fuzz(a) {return a} fun main(args) {_=bar() _=fuzz(a)} }");
        failureInput("class Foo { fun bar() {a = 1 + 2 return a} fun fuzz(a) {return a} fun main(args) {_=bar(b) _=fuzz(a)} }");
    }

    @Test
    public void testFunctionStatement() {
        autumnFixture.rule = grammar.root;
        successInput("class Foo { fun bar() { a = 1 return a } }");
        successInput("class Foo { fun bar() { a = 1 return a } }");
    }
}
