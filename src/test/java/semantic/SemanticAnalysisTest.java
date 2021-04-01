package semantic;

import AST.ASTNode;
import norswap.autumn.AutumnTestFixture;
import norswap.autumn.positions.LineMapString;
import norswap.uranium.Reactor;
import norswap.uranium.UraniumTestFixture;
import norswap.utils.visitors.Walker;
import parser.KneghelGrammar;
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
        successInput("5655"); //TODO failure input
        successInput("-1");
    }

    @Test
    public void testDouble() {
        autumnFixture.rule = grammar.doub;
        successInput("5.");
        successInput("5.0");
        successInput("-5478.32"); //TODO failure input
    }

    @Test
    public void testBool() {
        autumnFixture.rule = grammar.bool;
        successInput("true");
        successInput("false"); //TODO failure input
        //failureInput("TRUE");
    }

    @Test
    public void testString() {
        autumnFixture.rule = grammar.string;
        successInput("\"abc\"");
        successInput("\" a b c \"");
        //failureInput("\"\"\""); //TODO failure input
    }

    @Test
    public void testBinaryExpression() {
        autumnFixture.rule = grammar.additionExpression;
        successInput("1+2");
        successInput("1*2");
        successInput("1 + 2 + 3 - 4");
        successInput("1 + 2 * 3 / 4 + 5");
        successInput("1 - 2 + 3");
        //successInput("-1 + 2");
        successInput("1 / 2+ 3");
        successInput("1 + 2 % 3");
        successInput("1.0 + 2 % 3.0");
//        successInput("\"a\" + 1"); // TODO to allow see TODO in arithmetic
        //failureInput("1 + a"); //TODO JE NE SAIS PAS COMMENT FAIRE DES FAILURES
    }

    @Test
    public void testUnaryExpression() {
        autumnFixture.rule = grammar.prefixExpression;
        successInput("-1");
        successInput("!true");
        successInput("- 2.0");
    }

    @Test
    public void testArrayMapAccess() {
        autumnFixture.rule = grammar.arrayMapAccessExpression;
        successInput("a[i]");
        successInput("a[1+2]");
        successInput("a[0]");
    }

    @Test
    public void testVariableDef() {
        autumnFixture.rule = grammar.variableDefinition;
        successInput("a=1");
        successInput("a = true");
        successInput("a = b");
        successInput("a=1");
        successInput("a = \"coucou\"");
        successInput("a=null");
        successInput("abc = 1");
        successInput("a = 1 - 2");
        successInput("a = 1 / 2");
        successInput("a = false || true && true");
        failureInput("if = 5");
        failureInput("true = 6");
        failureInput("1 = a");
        failureInput("a = //coucou");
    }

    @Test
    public void testIfStatement() {
        autumnFixture.rule = grammar.root;
        successInput("class Foo { fun bar() {if true {a = 1+2}} }");
        successInput("class Foo { fun bar(a) {if a == 1 { a=2 b=2 } else if a == true { a=3 } else if a == \"hello\" { a=\"world\" }} }");
        successInput("class Foo { fun bar(a) {if 1 == 2 { _ = print(a) } }}");
        successInput("class Foo { fun bar() {if true { a=2 } else { a=3 }} }");
    }

    @Test
    public void testWhileStatement() {
        autumnFixture.rule = grammar.root;
        successInput("class Foo { fun bar() {while 1 > 2 { a = b } }}");
        successInput("class Foo { fun bar(b) {while true { a = b + 1 }} }");
        successInput("class Foo { fun bar(a, b) {while a == b { if c == 1 {b = 2}} }}");
        successInput("class Foo { fun bar() {while true { a = foo(a, b)}} }");

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
        successInput("class Foo { fun bar() {a = 1 + 2 return a} fun fuzz(a) {return a} fun main(args) {_ = bar() _= fuzz(a)} }");
        failureInput("class Foo { fun bar() {a = 1 + 2 return a} fun fuzz(a) {return a} fun main(args) {_ = bar(b) _= fuzz(a)} }");
    }

    @Test
    public void testFunctionStatement() {
        autumnFixture.rule = grammar.root;
        successInput("class Foo { fun bar() { a = 1 return a } }");
        successInput("class Foo { fun bar() { a = 1 return a } }");
    }

    @Test
    public void testFunctionHeader() {
        autumnFixture.rule = grammar.functionHeader;
        successInput("fun foo(a,b)");
    }

    /*@Test
    public void testIfStatement(){
        successInput("if (true == false) 1 + 1 + 10000");
        successInput("if (true) 1 + 1 + 10000");
        failureInput("if (1 + 1) 1 + 1 + 10000");
        failureInput("if (5) 1 + 1 + 10000");
    }*/
}
