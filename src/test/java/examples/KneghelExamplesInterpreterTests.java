package examples;

import ast.KneghelNode;
import grammar.KneghelGrammar;
import org.testng.annotations.Test;
import semantic.SemanticAnalysis;
import interpreter.Interpreter;

import norswap.autumn.AutumnTestFixture;
import norswap.autumn.Grammar;
import norswap.autumn.ParseResult;
import norswap.autumn.positions.LineMapString;
import norswap.uranium.Reactor;
import norswap.uranium.SemanticError;
import norswap.utils.IO;
import norswap.utils.TestFixture;
import norswap.utils.data.wrappers.Pair;
import norswap.utils.visitors.Walker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;

import static org.testng.Assert.*;

public class KneghelExamplesInterpreterTests extends TestFixture {

    private final KneghelGrammar grammar = new KneghelGrammar();
    private final AutumnTestFixture autumnFixture = new AutumnTestFixture();

    {
        autumnFixture.runTwice = false;
        autumnFixture.bottomClass = this.getClass();
    }

    private Grammar.rule rule;

    private void check(String input, Object expectedReturn, String expectedOutput) {
        assertNotNull(rule, "You forgot to initialize the rule field.");
        check(rule, input, expectedReturn, expectedOutput);
    }

    private void check(String input, Object expectedReturn, String expectedOutput, ArrayList<String> args) {
        assertNotNull(rule, "You forgot to initialize the rule field.");
        check(rule, input, expectedReturn, expectedOutput, args);
    }

    private void check(Grammar.rule rule, String input, Object expectedReturn, String expectedOutput) {
        check(rule, input, expectedReturn, expectedOutput, null);
    }

    private void check(Grammar.rule rule, String input, Object expectedReturn, String expectedOutput, ArrayList<String> args) {
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
            throw new AssertionError(report);
        }

        Pair<String, Object> result = IO.captureStdout(() -> interpreter.interpret(root));
        assertEquals(result.b, expectedReturn);
        if (expectedOutput != null)
            assertEquals(result.a.replaceAll("\r\n", "\n"), expectedOutput);
        else
            assertEquals(result.a, "");
    }

    public static String readFile(String fileName) {
//        String[] files = new String[] {"Fibonacci.kneghel", "FizzBuzz.kneghel", "Prime.kneghel", "Sort.kneghel", "Uniq.kneghel"};

        String content = "";
        try {
            Path file = Path.of("src/examples/" + fileName);
            content = Files.readString(file);
        } catch (IOException e) {
            System.err.println("Error reading file.");
        }

        return content;
    }


    @Test
    public void testFibonacci() {
        rule = grammar.root;
        check(readFile("Fibonacci.kneghel"),
                null,
                "0\n1\n1\n2\n3\n5\n8\n13\n21\n",
                new ArrayList<String>(){{
                    add("9");
                }});
    }

    @Test
    public void testFizzBuzz() {
        rule = grammar.root;
        check(readFile("FizzBuzz.kneghel"),
                null,
                "1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\nFizz\nBuzz\n11\nFizz\n13\n14\nFizzBuzz\n16\n17\nFizz\n19\nBuzz\nFizz\n22\n23\nFizz\nBuzz\n26\nFizz\n28\n29\nFizzBuzz\n31\n32\nFizz\n34\nBuzz\nFizz\n37\n38\nFizz\nBuzz\n41\nFizz\n43\n44\nFizzBuzz\n46\n47\nFizz\n49\nBuzz\nFizz\n52\n53\nFizz\nBuzz\n56\nFizz\n58\n59\nFizzBuzz\n61\n62\nFizz\n64\nBuzz\nFizz\n67\n68\nFizz\nBuzz\n71\nFizz\n73\n74\nFizzBuzz\n76\n77\nFizz\n79\nBuzz\nFizz\n82\n83\nFizz\nBuzz\n86\nFizz\n88\n89\nFizzBuzz\n91\n92\nFizz\n94\nBuzz\nFizz\n97\n98\nFizz\nBuzz\n",
                new ArrayList<String>(){{
                    add("100");
                }});
    }

    @Test
    public void testPrime() {
        rule = grammar.root;
        check(readFile("Prime.kneghel"),
                null,
                "2\n3\n5\n7\n11\n13\n17\n19\n23\n29\n",
                new ArrayList<String>(){{
                    add("10");
                }});
    }

    @Test
    public void testSort() {
        rule = grammar.root;
        check(readFile("Sort.kneghel"),
                new ArrayList() {{
                    add(1);
                    add(3);
                    add(5); }},
                "1\n3\n5\n",
                new ArrayList<String>(){{
                    add("5");
                    add("1");
                    add("3");
                }});
    }

    @Test
    public void testUniq() {
        rule = grammar.root;
        check(readFile("Uniq.kneghel"),
                new ArrayList() {{
                    add(1);
                    add(3);
                    add(5); }},
                "1\n3\n5\n",
                new ArrayList<String>(){{
                    add("5");
                    add("1");
                    add("3");
                }});
    }
}
