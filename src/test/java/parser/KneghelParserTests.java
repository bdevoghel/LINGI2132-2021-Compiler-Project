package parser;

import AST.*;
import org.testng.annotations.Test;
import norswap.autumn.TestFixture;

import java.awt.*;
import java.util.*;

import static AST.BinaryOperator.*;
import static AST.UnaryOperator.*;

public class KneghelParserTests extends TestFixture {

    KneghelParser parser = new KneghelParser();

    @Test
    public void testInteger() {
        this.rule = parser.integer;
        success("1");
        success("1234");
        failure("a");
        successExpect("1", new IntegerNode(1));
        success("-1");
        success("- 1");
        failure("-a");
        successExpect("-1", new IntegerNode(-1));
        successExpect("- 1", new IntegerNode(-1));
    }

    @Test
    public void testDouble() {
        this.rule = parser.doub;
        success("1.0");
        success("1234.1234");
        failure("a");
        successExpect("1.0", new DoubleNode(1.0));
        successExpect("1.23e-10", new DoubleNode(1.23e-10));
        successExpect("1.23e+10", new DoubleNode(1.23e+10));
        successExpect("1.23E-10", new DoubleNode(1.23e-10));
        successExpect("1.0", new DoubleNode(1.0));
        success("-1.1234");
        success("- 1.1234");
        failure("- 1. 1234");
        failure("- 1 .1234");
        failure("- 1.23 e10");
        failure("- 1.23e 10");
        failure("-a");
        successExpect("-1.1234", new DoubleNode(-1.1234));
        successExpect("- 1.1234", new DoubleNode(-1.1234));
    }

    @Test
    public void testStrings() {
        this.rule = parser.string;
        successExpect("\"abc\"", new StringNode("abc"));
        successExpect("\" a b c \"", new StringNode(" a b c "));
        successExpect("\"\"", new StringNode(""));
        failure("\"\"\"");
//        success("\"\\\"\""); // TODO implement
//        failure("\"a\nb\""); // TODO implement
    }

    @Test
    public void testBooleans() {
        this.rule = parser.bool;
        success("true");
        success("false");
        failure("True");
        failure("False");
        failure("truex");
        failure("falsex");
        successExpect("true", new BooleanNode(true));
        successExpect("false", new BooleanNode(false));
    }

    @Test
    public void testValues() {
        this.rule = parser.prefixExpression; // value = choice(integer, bool, identifier, string) with (optional) prefix
        success("1");
        success("-1");
        successExpect("- 1", new UnaryExpressionNode(NEG, new IntegerNode(1)));
        success("a");
        success("-a");
        successExpect("- a", new UnaryExpressionNode(NEG, new IdentifierNode("a")));
        success("true");
        success("!true");
        successExpect("! true", new UnaryExpressionNode(NOT, new BooleanNode(true)));
        success("\"a\"");
//        failure("- \"a\""); // TODO make this fail
//        failure("! \"a\"");
    }

    @Test
    public void testComment() {
        this.rule = parser.ws;
        success("// comment");
        success("//comment");
        success("//");
        success("// comment\n");
        success("// \n");
        failure("/notcomment");
        success("/* comment */");
        success("/*comment*/");
        success("/* comment \n still comment */");
        success("/* comment // still comment */");
        success("/* comment \n still comment \n */");
        failure("/* ");
        failure("/* \n");
        success("// /* ");
        success("/* \n// */");
        success("// comment \n // comment\n");
    }

    @Test
    public void testSimpleVarDef(){
        this.rule = parser.variableDefinition;
        success("a = 1");
        success("a = true");
        success("a = b");
        success("a=1");
        success("abc = 1");
        success("a = 1 - 2");
        success("a = 1 / 2");
        failure("a = // hey");
        failure("1 = 1");
        failure("true = 1");
        failure("if = 1");
        successExpect("a = 1", new AssignmentNode(new IdentifierNode("a"), new IntegerNode(1)));
        successExpect("a = true", new AssignmentNode(new IdentifierNode("a"), new BooleanNode(true)));
    }

    @Test
    public void testSimpleAddition() {
        this.rule = parser.additionExpression;
        success("1+2");
        success("1 + 2");
        success("-1 + 2");
        success("-1 - 2");
        success("1 -2");
        failure("1 ++ 2");
        failure("1 + + 2");
        success("5 + -2");
        failure("5 + ");
        success("1 + 2 - 3 + 4 - 5");
        successExpect("1 + 2", new BinaryExpressionNode(new IntegerNode(1), ADD, new IntegerNode(2)));
        successExpect("1 - 2", new BinaryExpressionNode(new IntegerNode(1), SUBTRACT, new IntegerNode(2)));
    }

    @Test
    public void testSimpleMultiplication() {
        this.rule = parser.multiplicationExpression;
        success("1*2");
        success("1 * 2");
        success("-1 * 2");
        success("-1 / 2");
        success("1 /2");
        failure("1 ** 2");
        failure("1 * * 2");
        success("5 * -2");
        failure("5 * ");
        success("1 / 2 * 3 / 4 * 5");
        success("6 % 2");
        successExpect("1 * 2", new BinaryExpressionNode(new IntegerNode(1), MULTIPLY, new IntegerNode(2)));
        successExpect("1 / 2", new BinaryExpressionNode(new IntegerNode(1), DIVIDE, new IntegerNode(2)));
        successExpect("6 % 2", new BinaryExpressionNode(new IntegerNode(6), MODULO, new IntegerNode(2)));
    }

    @Test
    public void testSimpleMixedOperations() {
        this.rule = parser.additionExpression;
        success("1 + 2 + 3 - 4");
        success("1 + 2 * 3 / 4 + 5");
        success("1 - 2 + 3");
        success("-1 + 2");
        success("1 / 2+ 3");
        success("1 + 2 % 3");
        successExpect("1 + 2 % 3 * 4",
                new BinaryExpressionNode(
                        new IntegerNode(1),
                        ADD,
                        new BinaryExpressionNode(
                                new BinaryExpressionNode(
                                        new IntegerNode(2),
                                        MODULO,
                                        new IntegerNode(3)),
                                MULTIPLY,
                                new IntegerNode(4))));
        successExpect("1 + 2 % 3 + 4",
                new BinaryExpressionNode(
                        new BinaryExpressionNode(
                                new IntegerNode(1),
                                ADD,
                                new BinaryExpressionNode(
                                        new IntegerNode(2),
                                        MODULO,
                                        new IntegerNode(3))),
                        ADD,
                        new IntegerNode(4)));
    }

    @Test
    public void testAdvancedVarDef() {
        this.rule = parser.variableDefinition;
//        failure("a = true * 5 + \"coucou\"");
        successExpect("a = null", new AssignmentNode(new IdentifierNode("a"), null));
        successExpect("a = 1 + 2 * 3", new AssignmentNode(new IdentifierNode("a"), new BinaryExpressionNode(new IntegerNode(1), ADD, new BinaryExpressionNode(new IntegerNode(2), MULTIPLY, new IntegerNode(3)))));
        successExpect("a = false || true && true", new AssignmentNode(new IdentifierNode("a"), new BinaryExpressionNode(new BooleanNode(false), OR, new BinaryExpressionNode(new BooleanNode(true), AND, new BooleanNode(true)))));
//        successExpect("a = \"a\"+\"a\" - \"a\"", new AssignmentNode(new IdentifierNode("a"), new BinaryExpressionNode(new StringNode("a"), ADD, new StringNode("a"))));
    }

    @Test
    public void testExpression() {
        this.rule = parser.expression;
        success("1");
        success("a");
        success("true");
        success("\"x\"");

        success("a[i]");
        success("a[1 + 2 > 3 && true]");

        success("1 == 2");
        success("1== 2");
        success("1 ==2");
        success("1 != 2");
        success("1 > 2");
        success("1 < 2");
        success("1 >= 2");
        success("1 <= 2");

        success("a <= b");
        success("a != 1");
        success("a == \"hello\"");
        success("a + b * c");

        success("false && true");
        success("false || true");
        success("false && true || false");
        success("false || true && false");

        success("a || true && b");

        successExpect("a[i]", new ArrayMapAccessNode(new IdentifierNode("a"), new IdentifierNode("i")));
        successExpect("a[1 + 2]", new ArrayMapAccessNode(new IdentifierNode("a"), new BinaryExpressionNode(new IntegerNode(1), ADD, new IntegerNode(2))));
        successExpect("1 + 2 <= 3 * 4", new BinaryExpressionNode(new BinaryExpressionNode(new IntegerNode(1), ADD, new IntegerNode(2)), LESS_OR_EQUAL, new BinaryExpressionNode(new IntegerNode(3), MULTIPLY, new IntegerNode(4))));
        successExpect("false || 1 + 2 * 3 <= 4 && true",
                new BinaryExpressionNode(
                        new BooleanNode(false),
                        OR,
                        new BinaryExpressionNode(
                                new BinaryExpressionNode(
                                        new BinaryExpressionNode(
                                                new IntegerNode(1),
                                                ADD,
                                                new BinaryExpressionNode(
                                                        new IntegerNode(2),
                                                        MULTIPLY,
                                                        new IntegerNode(3))),
                                        LESS_OR_EQUAL,
                                        new IntegerNode(4)),
                                AND ,
                                new BooleanNode(true))));
    }

    @Test
    public void testConditions() {
        this.rule = parser.ifStatement;
        successExpect("if 1 == 2 { a=3 }",
                new IfStatementNode(
                        new BinaryExpressionNode(new IntegerNode(1), EQUAL, new IntegerNode(2)),
                        Arrays.asList(new AssignmentNode(new IdentifierNode("a"), new IntegerNode(3)))));
        successExpect("if 1 == 2 { _ = print(a) }",
                new IfStatementNode(
                        new BinaryExpressionNode(new IntegerNode(1), EQUAL, new IntegerNode(2)),
                        Arrays.asList(new AssignmentNode(new IdentifierNode("_"), new FunctionCallNode(new IdentifierNode("print"), new FunctionArgumentsNode(Arrays.asList(new IdentifierNode("a"))))))));
        successExpect("if true { a=2 } else { a=3 }",
                new IfStatementNode(
                        new BooleanNode(true),
                        Arrays.asList(new AssignmentNode(new IdentifierNode("a"), new IntegerNode(2))),
                        Arrays.asList(new AssignmentNode(new IdentifierNode("a"), new IntegerNode(3)))));
        successExpect("if a == 1 { a=2 } else if a { a=3 }",
                new IfStatementNode(
                        new BinaryExpressionNode(new IdentifierNode("a"), EQUAL, new IntegerNode(1)),
                        Arrays.asList(new AssignmentNode(new IdentifierNode("a"), new IntegerNode(2))),
                        Arrays.asList(new IfStatementNode(
                                new IdentifierNode("a"),
                                Arrays.asList(
                                        new AssignmentNode(
                                            new IdentifierNode("a"),
                                            new IntegerNode(3)))))));
        success("if a == 1 { a=2 } else if a == true { a=3 } else if a == \"hello\" { a=\"world\" }");
        successExpect("if a == 1 { a=2 } else if a == true { a=3 } else if a == \"hello\" { a=\"world\" } else { a = null }",
                new IfStatementNode(
                        new BinaryExpressionNode(new IdentifierNode("a"), EQUAL, new IntegerNode(1)),
                        Arrays.asList(new AssignmentNode(new IdentifierNode("a"), new IntegerNode(2))),
                        Arrays.asList(new IfStatementNode(
                                new BinaryExpressionNode(new IdentifierNode("a"), EQUAL, new BooleanNode(true)),
                                Arrays.asList(new AssignmentNode(new IdentifierNode("a"), new IntegerNode(3))),
                                Arrays.asList(new IfStatementNode(
                                        new BinaryExpressionNode(new IdentifierNode("a"), EQUAL, new StringNode("hello")),
                                        Arrays.asList(new AssignmentNode(new IdentifierNode("a"), new StringNode("world"))),
                                        Arrays.asList(new AssignmentNode(new IdentifierNode("a"), null))))))));
        success("if a == 1 { a=2 b=2 } else if a == true { a=3 } else if a == \"hello\" { a=\"world\" }");
        failure("if a == 1 a=2 b=2 else if a == true { a=3 } else if a == \"hello\" { a=\"world\" }");
        failure("if { a == 1 } { a=2 } else if a == true { a=3 } else if a == \"hello\" { a=\"world\" }");
    }

    @Test
    public void testWhileLoops() {
        this.rule = parser.whileStatement;
        successExpect("while 1 > 2 { a = b }",
                new WhileStatementNode(
                        new BinaryExpressionNode(new IntegerNode(1), GREATER_THAN, new IntegerNode(2)),
                        Arrays.asList(new AssignmentNode(new IdentifierNode("a"), new IdentifierNode("b")))));
        successExpect("while true { a = b + 1 }",
                new WhileStatementNode(
                        new BooleanNode(true),
                        Arrays.asList(
                                new AssignmentNode(new IdentifierNode("a"),
                                        new BinaryExpressionNode(new IdentifierNode("b"), ADD, new IntegerNode(1))))));
        successExpect("while 1!=2 {a = 1}",
                new WhileStatementNode(
                        new BinaryExpressionNode(new IntegerNode(1), NOT_EQUAL, new IntegerNode(2)),
                        Arrays.asList(new AssignmentNode(new IdentifierNode("a"), new IntegerNode(1)))));
        successExpect("while a == b { if c == 1 {b = 2}}",
                new WhileStatementNode(
                        new BinaryExpressionNode(new IdentifierNode("a"), EQUAL, new IdentifierNode("b")),
                        Arrays.asList(
                                new IfStatementNode(
                                    new BinaryExpressionNode(new IdentifierNode("c"), EQUAL, new IntegerNode(1)),
                                    Arrays.asList(new AssignmentNode(new IdentifierNode("b"), new IntegerNode(2)))))));
        successExpect("while true { a = foo(a, b)}",
                new WhileStatementNode(
                        new BooleanNode(true),
                        Arrays.asList(new AssignmentNode(
                                new IdentifierNode("a"),
                                new FunctionCallNode(
                                        new IdentifierNode("foo"),
                                        new FunctionArgumentsNode(Arrays.asList(new IdentifierNode("a"), new IdentifierNode("b"))))))));
    }

    @Test
    public void testFunctions() {
        this.rule = parser.functionStatement;
        successExpect("fun bar() { a = 1 return a }",
                new FunctionStatementNode(
                        new IdentifierNode("bar"),
                        new FunctionArgumentsNode(Arrays.asList()),
                        Arrays.asList(new AssignmentNode(new IdentifierNode("a"), new IntegerNode(1)), new ReturnStatementNode(new IdentifierNode("a")))));
        successExpect("fun bar() { return 1 }",
                new FunctionStatementNode(
                        new IdentifierNode("bar"),
                        new FunctionArgumentsNode(Arrays.asList()),
                        Arrays.asList(new ReturnStatementNode(new IntegerNode(1)))));
        successExpect("fun foo(a, b) { c = a + b \n d = c * c \n return d }",
                new FunctionStatementNode(
                        new IdentifierNode("foo"),
                        new FunctionArgumentsNode(Arrays.asList(new IdentifierNode("a"), new IdentifierNode("b"))),
                        Arrays.asList(
                                new AssignmentNode(new IdentifierNode("c"), new BinaryExpressionNode(new IdentifierNode("a"), ADD, new IdentifierNode("b"))),
                                new AssignmentNode(new IdentifierNode("d"), new BinaryExpressionNode(new IdentifierNode("c"), MULTIPLY, new IdentifierNode("c"))),
                                new ReturnStatementNode(new IdentifierNode("d")))));
    }

    @Test
    public void testArrayMapAccess() {
        this.rule = parser.arrayMapAccessExpression;
        successExpect("a[i]", new ArrayMapAccessNode(new IdentifierNode("a"), new IdentifierNode("i")));
        successExpect("a[0]", new ArrayMapAccessNode(
                new IdentifierNode("a"), new IntegerNode(0)));
        successExpect("a[1+2]", new ArrayMapAccessNode(
                new IdentifierNode("a"),
                new BinaryExpressionNode(
                        new IntegerNode(1), ADD, new IntegerNode(2))));
    }

    @Test
    public void testFunctionCalls() {
        this.rule = parser.functionCallExpression;
        successExpect("foo(a, b)",
                new FunctionCallNode(
                        new IdentifierNode("foo"),
                        new FunctionArgumentsNode(Arrays.asList(new IdentifierNode("a"), new IdentifierNode("b")))));
        successExpect("foo(b)",
                new FunctionCallNode(
                        new IdentifierNode("foo"),
                        new FunctionArgumentsNode(Arrays.asList( new IdentifierNode("b")))));
        successExpect("foo()",
                new FunctionCallNode(
                        new IdentifierNode("foo"),
                        new FunctionArgumentsNode(Collections.emptyList())));
    }

    @Test
    public void testClasses() {
        this.rule = parser.classStatement;
        successExpect("class Foo {}", new ClassStatementNode(new IdentifierNode("Foo"), Arrays.asList()));
        successExpect("class Foo { fun bar() { return 1 } }",
                new ClassStatementNode(
                        new IdentifierNode("Foo"),
                        Arrays.asList(
                                new FunctionStatementNode(
                                        new IdentifierNode("bar"),
                                        new FunctionArgumentsNode(Arrays.asList()),
                                        Arrays.asList(new ReturnStatementNode(new IntegerNode(1)))))));
    }

    @Test
    public void testClassAsRoot() {
        this.rule = parser.root;
        failure("a = 1 + 2");
        failure("fun bar() { return 1 }");
        successExpect("class Foo {}", new ClassStatementNode(new IdentifierNode("Foo"), Arrays.asList()));
        successExpect("class Foo { fun bar() { return 1 } }",
                new ClassStatementNode(
                        new IdentifierNode("Foo"),
                        Arrays.asList(
                                new FunctionStatementNode(
                                        new IdentifierNode("bar"),
                                        new FunctionArgumentsNode(Arrays.asList()),
                                        Arrays.asList(new ReturnStatementNode(new IntegerNode(1)))))));
    }

//    @Test
//    public void testPrintCalls() {
//        this.rule = parser.printStatement;
//        successExpect("print(a)",
//                new PrintStatementNode(
//                        new IdentifierNode("a")));
//        successExpect("print(\"a\")",
//                new PrintStatementNode(
//                        new StringNode("a")));
//        successExpect("print(identifier)",
//                new PrintStatementNode(
//                        new IdentifierNode("identifier")));
//        successExpect("print(\"identifier\")",
//                new PrintStatementNode(
//                        new StringNode("identifier")));
//    }

//    @Test
//    public void testParsingString(){
//        this.rule = parser.parseStringToInt;
//        successExpect("int(\"4\")", new ParsingNode(new StringNode("4")));
//        successExpect("int(\"57389\")",
//                new ParsingNode(new StringNode("57389")));
//    }
}

