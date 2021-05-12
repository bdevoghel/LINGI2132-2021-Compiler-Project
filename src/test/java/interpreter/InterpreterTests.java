package interpreter;

import norswap.autumn.AutumnTestFixture;
import norswap.autumn.Grammar;
import norswap.autumn.Grammar.rule;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.LineMapString;

import semantic.SemanticAnalysis;
import grammar.KneghelGrammar;
import ast.KneghelNode;
import interpreter.Interpreter;
import interpreter.Null;

import norswap.uranium.Reactor;
import norswap.uranium.SemanticError;
import norswap.utils.IO;
import norswap.utils.TestFixture;
import norswap.utils.data.wrappers.Pair;
import norswap.utils.visitors.Walker;

import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.Set;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;

public final class InterpreterTests extends TestFixture {

    // TODO peeling

    // ---------------------------------------------------------------------------------------------

    private final KneghelGrammar grammar = new KneghelGrammar();
    private final AutumnTestFixture autumnFixture = new AutumnTestFixture();

    {
        autumnFixture.runTwice = false;
        autumnFixture.bottomClass = this.getClass();
    }

    // ---------------------------------------------------------------------------------------------

    private Grammar.rule rule;

    // ---------------------------------------------------------------------------------------------

    private void check (String input, Object expectedReturn) {
        assertNotNull(rule, "You forgot to initialize the rule field.");
        check(rule, input, expectedReturn, null);
    }

    // ---------------------------------------------------------------------------------------------

    private void check (String input, Object expectedReturn, String expectedOutput) {
        assertNotNull(rule, "You forgot to initialize the rule field.");
        check(rule, input, expectedReturn, expectedOutput);
    }

    // ---------------------------------------------------------------------------------------------

    private void check (rule rule, String input, Object expectedReturn, String expectedOutput) {
        // TODO
        // (1) write proper parsing tests
        // (2) write some kind of automated runner, and use it here

        autumnFixture.rule = rule;
        ParseResult parseResult = autumnFixture.success(input);
        Object o = parseResult.topValue();
        KneghelNode root = (KneghelNode) o;

        Reactor reactor = new Reactor();
        Walker<KneghelNode> walker = SemanticAnalysis.createWalker(reactor);
        Interpreter interpreter = new Interpreter(reactor);
        walker.walk(root);
        reactor.run();
        Set<SemanticError> errors = reactor.errors();

        if (!errors.isEmpty()) {
            LineMapString map = new LineMapString("<test>", input);
            String report = reactor.reportErrors(it ->
                    it.toString() + " (" + ((KneghelNode) it).span.startString(map) + ")");
            //            String tree = AttributeTreeFormatter.format(root, reactor,
            //                    new ReflectiveFieldWalker<>(SighNode.class, PRE_VISIT, POST_VISIT));
            //            System.err.println(tree);
            throw new AssertionError(report);
        }

        Pair<String, Object> result = IO.captureStdout(() -> interpreter.interpret(root));
        assertEquals(result.b, expectedReturn);
        if (expectedOutput != null) assertEquals(result.a, expectedOutput);
    }

    // ---------------------------------------------------------------------------------------------

    private void checkExpr (String input, Object expectedReturn, String expectedOutput) {
//        rule = grammar.root;
        check(input, expectedReturn, expectedOutput);
    }

    // ---------------------------------------------------------------------------------------------

    private void checkExpr (String input, Object expectedReturn) {
//        rule = grammar.return_stmt;
        check(input, expectedReturn);
    }

    // ---------------------------------------------------------------------------------------------

    private void checkThrows (String input, Class<? extends Throwable> expected) {
        assertThrows(expected, () -> check(input, null));
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testLiteralsAndUnary () {
        rule = grammar.prefix_expression;
//        checkExpr("", null);
        checkExpr("42", 42L);
        checkExpr("42.0", 42.0d);
        checkExpr("\"hello\"", "hello");
//        checkExpr("(42)", 42L);
        checkExpr("[1, 2, 3]", new Object[]{1L, 2L, 3L});
        checkExpr("true", true);
        checkExpr("false", false);
        checkExpr("null", Null.INSTANCE);
        checkExpr("!false", true);
        checkExpr("!true", false);
        checkExpr("!!true", true);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testNumericBinary () {
        rule = grammar.add_expr;
        checkExpr("1 + 2", 3L);
        checkExpr("2 - 1", 1L);
        checkExpr("2 * 3", 6L);
        checkExpr("2 / 3", 0L);
        checkExpr("3 / 2", 1L);
        checkExpr("2 % 3", 2L);
        checkExpr("3 % 2", 1L);

        checkExpr("1.0 + 2.0", 3.0d);
        checkExpr("2.0 - 1.0", 1.0d);
        checkExpr("2.0 * 3.0", 6.0d);
        checkExpr("2.0 / 3.0", 2d / 3d);
        checkExpr("3.0 / 2.0", 3d / 2d);
        checkExpr("2.0 % 3.0", 2.0d);
        checkExpr("3.0 % 2.0", 1.0d);

        checkExpr("1 + 2.0", 3.0d);
        checkExpr("2 - 1.0", 1.0d);
        checkExpr("2 * 3.0", 6.0d);
        checkExpr("2 / 3.0", 2d / 3d);
        checkExpr("3 / 2.0", 3d / 2d);
        checkExpr("2 % 3.0", 2.0d);
        checkExpr("3 % 2.0", 1.0d);

        checkExpr("1.0 + 2", 3.0d);
        checkExpr("2.0 - 1", 1.0d);
        checkExpr("2.0 * 3", 6.0d);
        checkExpr("2.0 / 3", 2d / 3d);
        checkExpr("3.0 / 2", 3d / 2d);
        checkExpr("2.0 % 3", 2.0d);
        checkExpr("3.0 % 2", 1.0d);

//        checkExpr("2 * (4-1) * 4.0 / 6 % (2+1)", 1.0d);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testOtherBinary () {
        rule = grammar.expression;
        checkExpr("true  && true",  true);
        checkExpr("true  || true",  true);
        checkExpr("true  || false", true);
        checkExpr("false || true",  true);
        checkExpr("false && true",  false);
        checkExpr("true  && false", false);
        checkExpr("false && false", false);
        checkExpr("false || false", false);

//        checkExpr("1 + \"a\"", "1a");
//        checkExpr("\"a\" + 1", "a1");
//        checkExpr("\"a\" + true", "atrue");

        checkExpr("1 == 1", true);
        checkExpr("1 == 2", false);
        checkExpr("1.0 == 1.0", true);
        checkExpr("1.0 == 2.0", false);
        checkExpr("true == true", true);
        checkExpr("false == false", true);
        checkExpr("true == false", false);
        checkExpr("1 == 1.0", true);
//        checkExpr("[1] == [1]", false);

        checkExpr("1 != 1", false);
        checkExpr("1 != 2", true);
        checkExpr("1.0 != 1.0", false);
        checkExpr("1.0 != 2.0", true);
        checkExpr("true != true", false);
        checkExpr("false != false", false);
        checkExpr("true != false", true);
        checkExpr("1 != 1.0", false);

        checkExpr("\"hi\" != \"hi2\"", true);
//        checkExpr("[1] != [1]", true);

        // test short circuit
//        checkExpr("true || print(\"x\") == \"y\"", true, "");
//        checkExpr("false && print(\"x\") == \"y\"", false, "");
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testVarDecl () {
        rule = grammar.statements;
//        check("i = 1 return i", 1L);
//        check("x = 2.0 return x", 2d);
//
////        check("var x: Int = 0; return x = 3", 3L);
////        check("var x: String = \"0\"; return x = \"S\"", "S");
//
//        // implicit conversions
////        check("var x: Float = 1; x = 2; return x", 2.0d);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testRootAndBlock () {
        rule = grammar.root;
//        check("return", null);
//        check("return 1", 1L);
//        check("return 1 return 2", 1L);
//
//        check("print(\"a\")", null, "a\n");
//        check("print(\"a\" + 1)", null, "a1\n");
//        check("print(\"a\") print(\"b\")", null, "a\nb\n");
//
//        check("{ print(\"a\") print(\"b\") }", null, "a\nb\n");
//
//        check(
//                "x = 1" +
//                        "{ print(x) x = 2 print(\"\" + x) }" +
//                        "print(\"\" + x)",
//                null, "1\n2\n1\n");
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testCalls () {
//        check(
//                "fun add (a, b) { return a + b } " +
//                        "return add(4, 7)",
//                11L);

//        HashMap<String, Object> point = new HashMap<>();
//        point.put("x", 1L);
//        point.put("y", 2L);
//
//        check(
//                "struct Point { var x: Int; var y: Int }" +
//                        "return $Point(1, 2)",
//                point);

//        check("str = null return print(str + \"hello\")", "hello", "hello\n");
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testArrayStructAccess () {
//        checkExpr("[1][0]", 1L);
//        checkExpr("[1.0][0]", 1d);
//        checkExpr("[1, 2][1]", 2L);

        // TODO check that this fails (& maybe improve so that it generates a better message?)
        // or change to make it legal (introduce a top type, and make it a top type array if thre
        // is no inference context available)
        // checkExpr("[].length", 0L);
//        checkExpr("len([1])", 1L);
//        checkExpr("len([1, 2])", 2L);
//
//        checkThrows("array = null return array[0]", NullPointerException.class);
//        checkThrows("array = null return len(array)", NullPointerException.class);

//        check("var x: Int[] = [0, 1]; x[0] = 3; return x[0]", 3L);
//        checkThrows("var x: Int[] = []; x[0] = 3; return x[0]",
//                ArrayIndexOutOfBoundsException.class);
//        checkThrows("var x: Int[] = null; x[0] = 3",
//                NullPointerException.class);
//
//        check(
//                "struct P { var x: Int; var y: Int }" +
//                        "return $P(1, 2).y",
//                2L);
//
//        checkThrows(
//                "struct P { var x: Int; var y: Int }" +
//                        "var p: P = null;" +
//                        "return p.y",
//                NullPointerException.class);
//
//        check(
//                "struct P { var x: Int; var y: Int }" +
//                        "var p: P = $P(1, 2);" +
//                        "p.y = 42;" +
//                        "return p.y",
//                42L);
//
//        checkThrows(
//                "struct P { var x: Int; var y: Int }" +
//                        "var p: P = null;" +
//                        "p.y = 42",
//                NullPointerException.class);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testIfWhile () {
        rule = grammar.statements;
        check("a = 0 if true {a = 1} else {a = 2}", null);
        check("a = 0 while a<=2 { a = a + 1}", null);
//        check("if true {return 1} else {return 2}", 1L);
//        check("if false {return 1} else {return 2}", 2L);
//        check("if false {return 1} else if true {return 2} else {return 3} ", 2L);
//        check("if false {return 1} else if false {return 2} else {return 3} ", 3L);
//
//        check("i = 0 while i < 3 { print(\"\" + i) i = i + 1 } ", null, "0\n1\n2\n");
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testInference () {
//        check("array = []", null);
//        check("array = []", null);
//        check("fun use_array (array) {} fun main(args) {use_array([])}", null);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testTypeAsValues () {
//        check("struct S{} ; return \"\"+ S", "S");
//        check("struct S{} ; var type: Type = S ; return \"\"+ type", "S");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testUnconditionalReturn()
    {
//        check("fun f(): Int { if (true) return 1 else return 2 } ; return f()", 1L);
    }

    // ---------------------------------------------------------------------------------------------

    // NOTE(norswap): Not incredibly complete, but should cover the basics.
}