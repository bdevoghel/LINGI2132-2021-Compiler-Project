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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;

public final class InterpreterTests extends TestFixture {


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
        check(rule, input, expectedReturn, expectedOutput, null);
    }

    private void check (rule rule, String input, Object expectedReturn, String expectedOutput, ArrayList<String> args) {

        autumnFixture.rule = rule;
        ParseResult parseResult = autumnFixture.success(input);
        KneghelNode root = parseResult.topValue();

        Reactor reactor = new Reactor();
        Walker<KneghelNode> walker = args == null
                ? SemanticAnalysis.createWalker(reactor)
                : SemanticAnalysis.createWalker(reactor, args);
        Interpreter interpreter = args == null
                ? new Interpreter(reactor)
                : new Interpreter(reactor, args);
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
        if (expectedOutput != null)
            assertEquals(result.a.replaceAll("\r\n", "\n"), expectedOutput);
    }

    // ---------------------------------------------------------------------------------------------

    private void checkExpr (String input, Object expectedReturn, String expectedOutput) {
        rule = grammar.root;
        check("class generic { fun main(args) {return " + input + "}}", expectedReturn, expectedOutput);
    }

    // ---------------------------------------------------------------------------------------------

    private void checkExpr (String input, Object expectedReturn) {
        rule = grammar.root;
        check("class generic { fun main(args) {return " + input + "}}", expectedReturn);
    }

    // ---------------------------------------------------------------------------------------------

    private void checkStmts (String input, Object expectedReturn, String expectedOutput) {
        rule = grammar.root;
        check("class generic { fun main(args) {" + input + "}}", expectedReturn, expectedOutput);
    }

    // ---------------------------------------------------------------------------------------------

    private void checkStmts (String input, Object expectedReturn) {
        rule = grammar.root;
        check("class generic { fun main(args) {" + input + "}}", expectedReturn);
    }

    // ---------------------------------------------------------------------------------------------

    private void checkFunctions (String input, Object expectedReturn, String expectedOutput) {
        rule = grammar.root;
        check("class generic {" + input + "}", expectedReturn, expectedOutput);
    }

    // ---------------------------------------------------------------------------------------------

    private void checkFunctions (String input, Object expectedReturn) {
        rule = grammar.root;
        check("class generic {" + input + "}", expectedReturn);
    }

    // ---------------------------------------------------------------------------------------------

    private void checkThrows (String input, Class<? extends Throwable> expected) {
        assertThrows(expected, () -> check(input, null));
    }

    // ---------------------------------------------------------------------------------------------

    private void checkStmtThrows (String input, Class<? extends Throwable> expected) {
        rule = grammar.root;
        assertThrows(expected, () -> check("class generic { fun main(args) {" + input + "}}", null));
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testLiteralsAndUnary () {
        checkExpr("42", 42L);
        checkExpr("42.0", 42.0d);
        checkExpr("\"hello\"", "hello");
        checkExpr("(42)", 42L);
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

        checkExpr("2 * (4-1) * 4.0 / 6 % (2+1)", 1.0d);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testOtherBinary () {
        checkExpr("true  && true",  true);
        checkExpr("true  || true",  true);
        checkExpr("true  || false", true);
        checkExpr("false || true",  true);
        checkExpr("false && true",  false);
        checkExpr("true  && false", false);
        checkExpr("false && false", false);
        checkExpr("false || false", false);

        // TODO
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

        checkExpr("1 != 1", false);
        checkExpr("1 != 2", true);
        checkExpr("1.0 != 1.0", false);
        checkExpr("1.0 != 2.0", true);
        checkExpr("true != true", false);
        checkExpr("false != false", false);
        checkExpr("true != false", true);
        checkExpr("1 != 1.0", false);

        checkExpr("\"hi\" != \"hi2\"", true);

        // test short circuit
        checkExpr("true || print(\"x\") == \"y\"", true, "");
        checkExpr("false && print(\"x\") == \"y\"", false, "");
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testParseInt () {
        checkStmts("str=\"1\" i=int(str) return i", 1);
        checkStmts("str=\"42\" i=int(str) return i", 42);
        // TODO add check throws
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testAssignments () {
        checkStmts("a = 1             return a", 1L);
        checkStmts("a = 1+1           return a", 2L);
        checkStmts("a = 0   a=a+1     return a", 1L);
        checkStmts("a = 1  b=a  a=b+1 return a", 2L);

    }

    // ---------------------------------------------------------------------------------------------

    // TODO implement test on args
//    @Test
//    public void testStatementsInMain () {
////        checkStmts("return 1", 1L);
//        checkFunctions("fun main() {return args[0]}", "");
////        checkStmts("return args[0]", null);
//    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testRootAndBlock () {
        checkStmts("return", null);
        checkStmts("return 1", 1L);
        checkStmts("return 1 return 2", 1L);

        checkStmts("print(\"a\")", null, "a\n");
//        checkStmts("print(\"a\" + 1)", null, "a1\n"); // TODO to handle (check other test)
        checkStmts("print(\"a\") print(\"b\")", null, "a\nb\n");

        checkStmts("{ print(\"a\") print(\"b\") }", null, "a\nb\n");

        checkStmts("x = 1" +
                        "if x>0 { print(x) x = 2 print(\"\" + x) }" +
                        "print(\"\" + x)",
                null, "1\n2\n2\n");
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testCalls () {
        checkStmts(
                "fun add (a, b) { return a + b } " +
                        "return add(4, 7)",
                11L);

        checkStmts("str = null return print(str + \" hello\")", "null hello", "null hello\n");
        checkStmts("a = makeArray() return a", new ArrayList<>());


        checkStmts("a = makeArray() return a", new ArrayList<>());
        checkStmts("a = makeArray() a[0] = 1 return a", new ArrayList<>(Arrays.asList(1L)));
        checkStmts("a = makeArray() a[0] = 1 return a[0]", 1L);
        checkStmts("a = makeArray() a[4] = \"world\" return a", new ArrayList<>(Arrays.asList(null,null,null,null,"world")));
        checkStmts("a = makeArray() a[4] = \"world\" a[2] = \"hello\" return a", new ArrayList<>(Arrays.asList(null,null,"hello",null,"world")));
        checkStmts("a = makeArray() b=0 a[b] = 42 return a[b]", 42L);
        checkStmts("a = makeArray() b=14 a[b] = 42 return a[b]", 42L);

        checkStmts("a = makeDict() return a", new HashMap<>());
        checkStmts("a = makeDict() a=dictAdd(a, \"x\", 1) return a", new HashMap<>(){{put("x", 1L);}});
        checkStmts("a = makeDict() a=dictAdd(a, \"x\", 1) b=dictGet(a, \"x\") return b", 1L);
        checkStmts("a = makeArray() return len(a)", 0);
        checkStmts("a = makeDict() return len(a)", 0);
        checkStmts("a = \"b\" return len(a)", 1);

        checkStmtThrows("a = 1 return len(a)", IllegalCallerException.class);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testArrayDictAccess () {
        checkStmts("dict = makeDict() return dictGet(dictAdd(dict,42,\"life\"), 42)", "life");
        checkStmts("arr = makeArray() arr[4] = \"life\" return arr[4]", "life");

        checkExpr("len(makeArray())", 0);
        checkExpr("len(makeDict())", 0);

        checkStmtThrows("array = null return array[0]", NullPointerException.class);
        checkStmtThrows("array = null return len(array)", NullPointerException.class);

        checkStmts("a = makeArray() a[0] = 3 return a[0]", 3L);
        checkStmtThrows("a = makeArray() a[0] = 3 return a[1]", IndexOutOfBoundsException.class);
        checkStmtThrows("a = null a[0] = 3", NullPointerException.class);
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testIfWhile () {
        checkStmts("a = 0 if true {a = 1} else {a = 2}", null);
        checkStmts("a = 0 while a<=2 { a = 3}", null);
        checkStmts("a = 0 while a<=2 { a = a + 3}", null);
        checkStmts("a = 0 while a<=2 { a = a + 1}", null);
        checkStmts("if true {return 1} else {return 2}", 1L);
        checkStmts("if false {return 1} else {return 2}", 2L);
        checkStmts("if false {return 1} else if true {return 2} else {return 3} ", 2L);
        checkStmts("if false {return 1} else if false {return 2} else {return 3} ", 3L);

        checkStmts("i = 0 while i < 3 { print(\"\" + i) i = i + 1 } print(\"\" + i)", null, "0\n1\n2\n3\n");
    }

    // ---------------------------------------------------------------------------------------------

    @Test
    public void testInference () {
        checkFunctions("fun use_array (array) {} fun main(args) {use_array(makeArray())}", null);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testUnconditionalReturn() {
        checkStmts("fun f() { if true {return 1} else {return 2} } return f()", 1L);
    }
}
