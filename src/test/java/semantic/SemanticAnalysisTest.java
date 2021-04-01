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
    public void testPrime() {
        successInput("class Prime {\n" +
                "    fun isPrime(number) {\n" +
                "        if number <= 1 {\n" +
                "            return false\n" +
                "        }\n" +
                "        prime = true\n" +
                "        i = 2\n" +
                "        while i < number && prime {\n" +
                "            if number % i == 0 {\n" +
                "                prime = false\n" +
                "            }\n" +
                "            i = i + 1\n" +
                "        }\n" +
                "        return prime\n" +
                "    }\n" +
                "\n" +
                "    fun main(args) {\n" +
                "        N = int(args[0])\n" + //TODO args[0] not identifier bc it is arraymapaccessnode maybe make arraymapaccessnode extend identifier ?
                //"        N = int(args)\n" +
                "        current = 2\n" +
                "        count = 0\n" +
                "        while count < N {\n" +
                "            if isPrime(current) {\n" +
                "                _ = print(current)\n" +
                "                count = count + 1\n" +
                "            }\n" +
                "            current = current + 1\n" +
                "        }\n" +
                "        return 0\n" +
                "    }\n" +
                "}");
    }

    @Test
    public void testFizzBuzz() {
        successInput("class FizzBuzz {\n" +
                "    fun main(args) {\n" +
                "        i = 1\n" +
                "        while i<=100{\n" +
                "            if i%15==0{\n" +
                "                _= print(\"FizzBuzz\")\n" +
                "            } else if i %3 ==0 {\n" +
                "                _ =print(\"Fizz\")\n" +
                "            } else if i% 5== 0 {\n" +
                "                _=print(\" - Buzz\")\n" +
                "            } else {\n" +
                "                _ = print(i)\n" +
                "            }\n" +
                "\n" +
                "            i=i+1\n" +
                "        }\n" +
                "        return 0\n" +
                "    }\n" +
                "}");
    }

    @Test
    public void testFibonacci() {
        successInput("class Fibonacci {\n" +
                "\n" +
                "    /*\n" +
                "     * Recursive fibonacci function\n" +
                "     */\n" +
                "    fun fibonacci(a, b, N) {\n" +
                "        if N == 0 {\n" +
                "            return null // end of recursion\n" +
                "        }\n" +
                "        _ = print(a)\n" +
                "        return fibonacci(b, a+b, N-1)\n" +
                "    }\n" +
                "\n" +
                "    fun main(args) {\n" +
                "        N = int(args[0])\n" + //TODO same problem as above
               // "        N = int(args)\n" +
                //"        _ = fibonacci(0, 1, N)\n" + //TODO integer is not identifier ....
                "        zero = 0 \n" +
                "        one = 1 \n" +
                "        _ = fibonacci(zero, one, N)\n" +
                "        return 0\n" +
                "    }\n" +
                "}");
    }

    @Test
    public void testSort() {
        successInput("class Sort {\n" +
                "\n" +
                "    fun swap(a, i, j) {\n" +
                "        tmp = a[i]\n" +
                "        a[i] = a[j]\n" +
                "        a[j] = tmp\n" +
                "        return true\n" +
                "    }\n" +
                "\n" +
                "    fun sort(numbers) {\n" +
                "        i = 0\n" +
                "        while i < len(numbers) {\n" +
                "            j = i+1\n" +
                "            while j < len(numbers) {\n" +
                "                if numbers[i] > numbers[j] {\n" +
                "                    _ = swap(numbers, i, j)\n" +
                "                }\n" +
                "                j = j + 1\n" +
                "            }\n" +
                "            i = i + 1\n" +
                "        }\n" +
                "        return true\n" +
                "    }\n" +
                "\n" +
                "    fun main(args) {\n" +
                "        numbers = makeArray(len(args))\n" +
                "        i = 0\n" +
                "        while i < len(args) {\n" +
                "            numbers[i] = int(args[i])\n" +
                "            i = i + 1\n" +
                "        }\n" +
                "        _ = sort(numbers)\n" +
                "        i = 0\n" +
                "        while i < len(numbers) {\n" +
                "            _ = print(numbers[i])\n" +
                "            i = i + 1\n" +
                "        }\n" +
                "        return 0\n" +
                "    }\n" +
                "}\n");
    }

    @Test
    public void testUniq() {
        successInput("class Uniq {\n" +
                "\n" +
                "    fun main(args) {\n" +
                "        m = makeDict()\n" +
                "        i = 0\n" +
                "        while i < len(args) {\n" +
                "            if m[args[i]] == null {\n" +
                "                _ = print(args[i])\n" +
                "                m[args[i]] = true\n" +
                "            }\n" +
                "            i = i + 1\n" +
                "        }\n" +
                "        return 0\n" +
                "    }\n" +
                "}\n");
    }
}
