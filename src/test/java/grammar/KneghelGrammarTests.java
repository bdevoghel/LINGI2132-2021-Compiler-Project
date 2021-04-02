package grammar;

import AST.*;
import org.testng.annotations.Test;
import norswap.autumn.AutumnTestFixture;

import java.util.*;

import static AST.BinaryOperator.*;
import static AST.UnaryOperator.*;

public class KneghelGrammarTests extends AutumnTestFixture {

    KneghelGrammar grammar = new KneghelGrammar();

    @Test
    public void testInteger() {
        this.rule = grammar.integer;
        success("1");
        success("1234");
        failure("a");
        successExpect("1", new IntegerNode(null,1));
        success("-1");
        success("- 1");
        failure("-a");
        successExpect("-1", new IntegerNode(null,-1));
        successExpect("- 1", new IntegerNode(null, -1));
    }

    @Test
    public void testDouble() {
        this.rule = grammar.doub;
        success("1.0");
        success("1234.1234");
        successExpect("5.", new DoubleNode(null, 5.0));
        successExpect("5.0", new DoubleNode(null, 5.0));
        failure("5");
        successExpect("1.0", new DoubleNode(null, 1.0));
        successExpect("1.23e-10", new DoubleNode(null,1.23e-10));
        successExpect("1.23e+10", new DoubleNode(null, 1.23e+10));
        successExpect("1.23E-10", new DoubleNode(null, 1.23e-10));
        successExpect("1.0", new DoubleNode(null, 1.0));
        success("-1.1234");
        success("- 1.1234");
        failure("- 1. 1234");
        failure("- 1 .1234");
        failure("- 1.23 e10");
        failure("- 1.23e 10");
        failure("a");
        failure("-a");
        successExpect("-1.1234", new DoubleNode(null, -1.1234));
        successExpect("- 1.1234", new DoubleNode(null,-1.1234));
    }

    @Test
    public void testStrings() {
        this.rule = grammar.string;
        successExpect("\"abc\"", new StringNode(null,"abc"));
        successExpect("\" a b c \"", new StringNode(null," a b c "));
        successExpect("\"\"", new StringNode(null,""));
        failure("\"\"\"");
//        success("\"\\\"\""); // TODO implement
//        failure("\"a\nb\""); // TODO implement
    }

    @Test
    public void testBooleans() {
        this.rule = grammar.bool;
        success("true");
        success("false");
        failure("True");
        failure("False");
        failure("truex");
        failure("falsex");
        successExpect("true", new BooleanNode(null,true));
        successExpect("false", new BooleanNode(null,false));
    }

    @Test
    public void testValues() {
        this.rule = grammar.prefixExpression;
        success("1");
        success("-1");
        success("1.0");
        success("-1.0");
        successExpect("- 1", new UnaryExpressionNode(null, NEG, new IntegerNode(null,1)));
        success("a");
        success("-a");
        successExpect("- a", new UnaryExpressionNode(null,NEG, new IdentifierNode(null,"a")));
        success("true");
        success("!true");
        successExpect("! true", new UnaryExpressionNode(null,NOT, new BooleanNode(null,true)));
        success("\"a\"");
        success("- \"a\""); // to be dealt with in semantics
        success("! \"a\""); // to be dealt with in semantics
    }

    @Test
    public void testComment() {
        this.rule = grammar.ws;
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
        this.rule = grammar.variableDefinition;
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
        successExpect("a = 1", new AssignmentNode(null, new IdentifierNode(null,"a"), new IntegerNode(null,1)));
        successExpect("a = true", new AssignmentNode(null,new IdentifierNode(null,"a"), new BooleanNode(null,true)));
    }

    @Test
    public void testSimpleAddition() {
        this.rule = grammar.additionExpression;
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
        successExpect("1 + 2", new BinaryExpressionNode(null,new IntegerNode(null,1), ADD, new IntegerNode(null,2)));
        successExpect("1 - 2", new BinaryExpressionNode(null,new IntegerNode(null,1), SUBTRACT, new IntegerNode(null,2)));
    }

    @Test
    public void testSimpleMultiplication() {
        this.rule = grammar.multiplicationExpression;
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
        successExpect("1 * 2", new BinaryExpressionNode(null,new IntegerNode(null,1), MULTIPLY, new IntegerNode(null,2)));
        successExpect("1 / 2", new BinaryExpressionNode(null,new IntegerNode(null,1), DIVIDE, new IntegerNode(null,2)));
        successExpect("6 % 2", new BinaryExpressionNode(null,new IntegerNode(null,6), MODULO, new IntegerNode(null,2)));
    }

    @Test
    public void testSimpleMixedOperations() {
        this.rule = grammar.additionExpression;
        success("1 + 2 + 3 - 4");
        success("1 + 2 * 3 / 4 + 5");
        success("1 - 2 + 3");
        success("-1 + 2");
        success("1 / 2+ 3");
        success("1 + 2 % 3");
        successExpect("1 + 2 % 3 * 4",
                new BinaryExpressionNode(
                        null,
                        new IntegerNode(null,1),
                        ADD,
                        new BinaryExpressionNode(
                                null,
                                new BinaryExpressionNode(
                                        null,
                                        new IntegerNode(null,2),
                                        MODULO,
                                        new IntegerNode(null,3)),
                                MULTIPLY,
                                new IntegerNode(null,4))));
        successExpect("1 + 2 % 3 + 4",
                new BinaryExpressionNode(
                        null,
                        new BinaryExpressionNode(
                                null,
                                new IntegerNode(null,1),
                                ADD,
                                new BinaryExpressionNode(
                                        null,
                                        new IntegerNode(null,2),
                                        MODULO,
                                        new IntegerNode(null,3))),
                        ADD,
                        new IntegerNode(null,4)));
    }

    @Test
    public void testAdvancedVarDef() {
        this.rule = grammar.variableDefinition;
//        failure("a = true * 5 + \"coucou\"");
        successExpect("a = null", new AssignmentNode(null,new IdentifierNode(null,"a"), null));
        successExpect("a = 1 + 2 * 3", new AssignmentNode(null,new IdentifierNode(null,"a"), new BinaryExpressionNode(null,new IntegerNode(null,1), ADD, new BinaryExpressionNode(null,new IntegerNode(null,2), MULTIPLY, new IntegerNode(null,3)))));
        successExpect("a = false || true && true", new AssignmentNode(null,new IdentifierNode(null,"a"), new BinaryExpressionNode(null,new BooleanNode(null,false), OR, new BinaryExpressionNode(null,new BooleanNode(null,true), AND, new BooleanNode(null,true)))));
//        successExpect("a = \"a\"+\"a\" - \"a\"", new AssignmentNode(new IdentifierNode("a"), new BinaryExpressionNode(new StringNode("a"), ADD, new StringNode("a"))));
    }

    @Test
    public void testExpression() {
        this.rule = grammar.expression;
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

        successExpect("a[i]", new ArrayMapAccessNode(null, new IdentifierNode(null, "a"), new IdentifierNode(null,"i")));
        successExpect("a[1 + 2]", new ArrayMapAccessNode(null, new IdentifierNode(null, "a"), new BinaryExpressionNode(null, new IntegerNode(null,1), ADD, new IntegerNode(null,2))));
        successExpect("1 + 2 <= 3 * 4", new BinaryExpressionNode(null, new BinaryExpressionNode(null, new IntegerNode(null,1), ADD, new IntegerNode(null,2)), LESS_OR_EQUAL, new BinaryExpressionNode(null, new IntegerNode(null, 3), MULTIPLY, new IntegerNode(null,4))));
        successExpect("false || 1 + 2 * 3 <= 4 && true",
                new BinaryExpressionNode(
                        null,
                        new BooleanNode(null,false),
                        OR,
                        new BinaryExpressionNode(
                                null,
                                new BinaryExpressionNode(
                                        null,
                                        new BinaryExpressionNode(
                                                null,
                                                new IntegerNode(null,1),
                                                ADD,
                                                new BinaryExpressionNode(
                                                        null,
                                                        new IntegerNode(null,2),
                                                        MULTIPLY,
                                                        new IntegerNode(null,3))),
                                        LESS_OR_EQUAL,
                                        new IntegerNode(null,4)),
                                AND ,
                                new BooleanNode(null,true))));
    }

    @Test
    public void testConditions() {
        this.rule = grammar.ifStatement;
        successExpect("if 1 == 2 { a=3 }",
                new IfStatementNode(
                        null,
                        new BinaryExpressionNode(null,new IntegerNode(null,1), EQUAL, new IntegerNode(null,2)),
                        Arrays.asList(new AssignmentNode(null,new IdentifierNode(null,"a"), new IntegerNode(null,3)))));
        successExpect("if 1 == 2 { _ = print(a) }",
                new IfStatementNode(
                        null,
                        new BinaryExpressionNode(null,new IntegerNode(null,1), EQUAL, new IntegerNode(null,2)),
                        Arrays.asList(new AssignmentNode(null, new IdentifierNode(null, "_"), new FunctionCallNode(null,new IdentifierNode(null,"print"), new FunctionArgumentsNode(null,Arrays.asList(new IdentifierNode(null,"a"))))))));
        successExpect("if true { a=2 } else { a=3 }",
                new IfStatementNode(
                        null,
                        new BooleanNode(null,true),
                        Arrays.asList(new AssignmentNode(null,new IdentifierNode(null,"a"), new IntegerNode(null,2))),
                        Arrays.asList(new AssignmentNode(null,new IdentifierNode(null,"a"), new IntegerNode(null,3)))));
        successExpect("if a == 1 { a=2 } else if a { a=3 }",
                new IfStatementNode(
                        null,
                        new BinaryExpressionNode(null,new IdentifierNode(null,"a"), EQUAL, new IntegerNode(null,1)),
                        Arrays.asList(new AssignmentNode(null,new IdentifierNode(null,"a"), new IntegerNode(null,2))),
                        Arrays.asList(new IfStatementNode(
                                null,
                                new IdentifierNode(null,"a"),
                                Arrays.asList(
                                        new AssignmentNode(
                                                null,
                                            new IdentifierNode(null,"a"),
                                            new IntegerNode(null,3)))))));
        success("if a == 1 { a=2 } else if a == true { a=3 } else if a == \"hello\" { a=\"world\" }");
        successExpect("if a == 1 { a=2 } else if a == true { a=3 } else if a == \"hello\" { a=\"world\" } else { a = null }",
                new IfStatementNode(
                        null,
                        new BinaryExpressionNode(null,new IdentifierNode(null,"a"), EQUAL, new IntegerNode(null,1)),
                        Arrays.asList(new AssignmentNode(null,new IdentifierNode(null,"a"), new IntegerNode(null,2))),
                        Arrays.asList(new IfStatementNode(
                                null,
                                new BinaryExpressionNode(null,new IdentifierNode(null,"a"), EQUAL, new BooleanNode(null,true)),
                                Arrays.asList(new AssignmentNode(null,new IdentifierNode(null,"a"), new IntegerNode(null,3))),
                                Arrays.asList(new IfStatementNode(
                                        null,
                                        new BinaryExpressionNode(null,new IdentifierNode(null,"a"), EQUAL, new StringNode(null,"hello")),
                                        Arrays.asList(new AssignmentNode(null,new IdentifierNode(null,"a"), new StringNode(null,"world"))),
                                        Arrays.asList(new AssignmentNode(null,new IdentifierNode(null,"a"), null))))))));
        success("if a == 1 { a=2 b=2 } else if a == true { a=3 } else if a == \"hello\" { a=\"world\" }");
        failure("if a == 1 a=2 b=2 else if a == true { a=3 } else if a == \"hello\" { a=\"world\" }");
        failure("if { a == 1 } { a=2 } else if a == true { a=3 } else if a == \"hello\" { a=\"world\" }");
    }

    @Test
    public void testWhileLoops() {
        this.rule = grammar.whileStatement;
        successExpect("while 1 > 2 { a = b }",
                new WhileStatementNode(
                        null,
                        new BinaryExpressionNode(null,new IntegerNode(null,1), GREATER_THAN, new IntegerNode(null,2)),
                        Arrays.asList(new AssignmentNode(null,new IdentifierNode(null,"a"), new IdentifierNode(null,"b")))));
        successExpect("while true { a = b + 1 }",
                new WhileStatementNode(
                        null,
                        new BooleanNode(null,true),
                        Arrays.asList(
                                new AssignmentNode(null,new IdentifierNode(null,"a"),
                                        new BinaryExpressionNode(null,new IdentifierNode(null,"b"), ADD, new IntegerNode(null,1))))));
        successExpect("while 1!=2 {a = 1}",
                new WhileStatementNode(
                        null,
                        new BinaryExpressionNode(null,new IntegerNode(null,1), NOT_EQUAL, new IntegerNode(null,2)),
                        Arrays.asList(new AssignmentNode(null,new IdentifierNode(null,"a"), new IntegerNode(null,1)))));
        successExpect("while a == b { if c == 1 {b = 2}}",
                new WhileStatementNode(
                        null,
                        new BinaryExpressionNode(null,new IdentifierNode(null,"a"), EQUAL, new IdentifierNode(null,"b")),
                        Arrays.asList(
                                new IfStatementNode(
                                        null,
                                    new BinaryExpressionNode(null,new IdentifierNode(null,"c"), EQUAL, new IntegerNode(null,1)),
                                    Arrays.asList(new AssignmentNode(null,new IdentifierNode(null,"b"), new IntegerNode(null,2)))))));
        successExpect("while true { a = foo(a, b)}",
                new WhileStatementNode(
                        null,
                        new BooleanNode(null,true),
                        Arrays.asList(new AssignmentNode(
                                null,
                                new IdentifierNode(null,"a"),
                                new FunctionCallNode(
                                        null,
                                        new IdentifierNode(null,"foo"),
                                        new FunctionArgumentsNode(null,Arrays.asList(new IdentifierNode(null,"a"), new IdentifierNode(null,"b"))))))));
    }

    @Test
    public void testFunctions() {
        this.rule = grammar.functionStatement;
        successExpect("fun bar() { a = 1 return a }",
                new FunctionStatementNode(
                        null,
                        new IdentifierNode(null,"bar"),
                        Arrays.asList(),
                        Arrays.asList(new AssignmentNode(null,new IdentifierNode(null,"a"), new IntegerNode(null,1)), new ReturnStatementNode(null,new IdentifierNode(null,"a")))));
        successExpect("fun bar() { return 1 }",
                new FunctionStatementNode(
                        null,
                        new IdentifierNode(null,"bar"),
                        Arrays.asList(),
                        Arrays.asList(new ReturnStatementNode(null,new IntegerNode(null,1)))));
        successExpect("fun foo(a, b) { c = a + b \n d = c * c \n return d }",
                new FunctionStatementNode(
                        null,
                        new IdentifierNode(null,"foo"),
                        Arrays.asList(
                                new FunctionParameterNode(null, new IdentifierNode(null,"a")),
                                new FunctionParameterNode(null, new IdentifierNode(null,"b")) ),
                        Arrays.asList(
                                new AssignmentNode(null,new IdentifierNode(null,"c"), new BinaryExpressionNode(null,new IdentifierNode(null,"a"), ADD, new IdentifierNode(null,"b"))),
                                new AssignmentNode(null,new IdentifierNode(null,"d"), new BinaryExpressionNode(null,new IdentifierNode(null,"c"), MULTIPLY, new IdentifierNode(null,"c"))),
                                new ReturnStatementNode(null,new IdentifierNode(null,"d")))));
    }

    @Test
    public void testArrayMapAccess() {
        this.rule = grammar.arrayMapAccessExpression;
        successExpect("a[i]", new ArrayMapAccessNode(null,new IdentifierNode(null,"a"), new IdentifierNode(null,"i")));
        successExpect("a[0]", new ArrayMapAccessNode(
                null,
                new IdentifierNode(null,"a"), new IntegerNode(null,0)));
        successExpect("a[1+2]", new ArrayMapAccessNode(
                null,
                new IdentifierNode(null,"a"),
                new BinaryExpressionNode(
                        null,
                        new IntegerNode(null,1), ADD, new IntegerNode(null,2))));
    }

    @Test
    public void testFunctionCalls() {
        this.rule = grammar.functionCallExpression;
        successExpect("foo(a, b)",
                new FunctionCallNode(
                        null,
                        new IdentifierNode(null,"foo"),
                        new FunctionArgumentsNode(null,Arrays.asList(new IdentifierNode(null,"a"), new IdentifierNode(null,"b")))));
        successExpect("foo(1, b)",
                new FunctionCallNode(
                        null,
                        new IdentifierNode(null,"foo"),
                        new FunctionArgumentsNode(null,Arrays.asList(new IntegerNode(null,1), new IdentifierNode(null,"b")))));
        successExpect("foo(b)",
                new FunctionCallNode(
                        null,
                        new IdentifierNode(null,"foo"),
                        new FunctionArgumentsNode(null,Arrays.asList( new IdentifierNode(null,"b")))));
        successExpect("foo()",
                new FunctionCallNode(
                        null,
                        new IdentifierNode(null,"foo"),
                        new FunctionArgumentsNode(null,Collections.emptyList())));
    }

    @Test
    public void testClasses() {
        this.rule = grammar.classStatement;
        successExpect("class Foo {}", new ClassStatementNode(null,new IdentifierNode(null,"Foo"), Arrays.asList()));
        successExpect("class Foo { fun bar() { return 1 } }",
                new ClassStatementNode(
                        null,
                        new IdentifierNode(null, "Foo"),
                        Arrays.asList(
                                new FunctionStatementNode(
                                        null,
                                        new IdentifierNode(null,"bar"),
                                        Arrays.asList(),
                                        Arrays.asList(new ReturnStatementNode(null,new IntegerNode(null,1)))))));
    }

    @Test
    public void testClassAsRoot() {
        this.rule = grammar.root;
        failure("a = 1 + 2");
        failure("fun bar() { return 1 }");
        successExpect("class Foo {}", new ClassStatementNode(null,new IdentifierNode(null,"Foo"), Arrays.asList()));
        successExpect("class Foo { fun bar() { return 1 } }",
                new ClassStatementNode(
                        null,
                        new IdentifierNode(null,"Foo"),
                        Arrays.asList(
                                new FunctionStatementNode(
                                        null,
                                        new IdentifierNode(null,"bar"),
                                        Arrays.asList(),
                                        Arrays.asList(new ReturnStatementNode(null,new IntegerNode(null,1)))))));
    }
}

