package grammar;

import ast.*;
import org.testng.annotations.Test;
import norswap.autumn.AutumnTestFixture;

import java.util.*;

import static ast.BinaryOperator.*;
import static ast.UnaryOperator.*;

public class KneghelGrammarTests extends AutumnTestFixture {

    KneghelGrammar grammar = new KneghelGrammar();

    // TODO reorder first tests and rename them all
    @Test
    public void testClass() {
        this.rule = grammar.root;
        success("class Hello { }");
        successExpect("class Hello { fun World() {} }",
                new ClassNode(null,
                        "Hello",
                        Arrays.asList(
                                new FunDeclarationNode(null,
                                        "World",
                                        Arrays.asList(),
                                        new BlockNode(null, Arrays.asList()))
                        )));
    }

    @Test
    public void testFunctions() {
        this.rule = grammar.root;
        successExpect("class Hello { fun World() {a=1} }",
                new ClassNode(null,
                        "Hello",
                        Arrays.asList(
                                new FunDeclarationNode(null,
                                        "World",
                                        Arrays.asList(),
                                        new BlockNode(null, Arrays.asList(
                                                new AssignmentNode(null,
                                                        new ReferenceNode(null, "a"),
                                                        new IntLiteralNode(null, 1))
                                        )))
                        )));
        success("class Hello { fun World() {return 1 return 1} }");
        success("class Hello { fun World() {fun Other() {return \"other\"} return Other()} }");
        successExpect("class Hello { fun World() {return 1} }",
                new ClassNode(null,
                        "Hello",
                        Arrays.asList(
                                new FunDeclarationNode(null,
                                        "World",
                                        Arrays.asList(),
                                        new BlockNode(null, Arrays.asList(
                                                new ReturnNode(null, new IntLiteralNode(null, 1))
                                        )))
                        )));
        successExpect("class Hello { fun World(a, b) {return a} }",
                new ClassNode(null,
                        "Hello",
                        Arrays.asList(
                                new FunDeclarationNode(null,
                                        "World",
                                        Arrays.asList(
                                                new ParameterNode(null, "a"),
                                                new ParameterNode(null, "b")
                                        ),
                                        new BlockNode(null, Arrays.asList(
                                                new ReturnNode(null, new ReferenceNode(null, "a"))
                                        )))
                        )));
        successExpect("class Hello { fun World(a, b) {return a} fun main(args) {World(1, 2)} }",
                new ClassNode(null,
                        "Hello",
                        Arrays.asList(
                                new FunDeclarationNode(null,
                                        "World",
                                        Arrays.asList(
                                                new ParameterNode(null, "a"),
                                                new ParameterNode(null, "b")
                                        ),
                                        new BlockNode(null, Arrays.asList(
                                                new ReturnNode(null, new ReferenceNode(null, "a"))
                                        ))),
                                new FunDeclarationNode(null,
                                        "main",
                                        Arrays.asList(new ParameterNode(null, "args")),
                                        new BlockNode(null, Arrays.asList(
                                                new ExpressionStatementNode(null, new FunCallNode(null,
                                                        new ReferenceNode(null, "World"),
                                                        Arrays.asList(new IntLiteralNode(null, 1), new IntLiteralNode(null, 2))))
                                        )))
                        )));
        successExpect("class Hello { fun World(a) {return a+1} fun main(args) {a=2 return World(a)} }",
                new ClassNode(null,
                        "Hello",
                        Arrays.asList(
                                new FunDeclarationNode(null,
                                        "World",
                                        Arrays.asList(
                                                new ParameterNode(null, "a")
                                        ),
                                        new BlockNode(null, Arrays.asList(
                                                new ReturnNode(null, new BinaryExpressionNode(null,
                                                        new ReferenceNode(null, "a"),
                                                        ADD,
                                                        new IntLiteralNode(null, 1)))
                                        ))),
                                new FunDeclarationNode(null,
                                        "main",
                                        Arrays.asList(new ParameterNode(null, "args")),
                                        new BlockNode(null, Arrays.asList(
                                                new AssignmentNode(null,
                                                        new ReferenceNode(null, "a"),
                                                        new IntLiteralNode(null, 2)),
                                                new ReturnNode(null, new FunCallNode(null,
                                                        new ReferenceNode(null, "World"),
                                                        Arrays.asList(new ReferenceNode(null, "a"))))
                                        )))
                        )));
    }

    @Test
    public void testInteger() {
        this.rule = grammar.integer;
        success("1");
        success("1234");
        failure("a");
        successExpect("1", new IntLiteralNode(null,1));
        success("-1");
        success("- 1");
        failure("-a");
        successExpect("-1", new IntLiteralNode(null,-1));
        successExpect("- 1", new IntLiteralNode(null, -1));
    }

    @Test
    public void testFloating() {
        this.rule = grammar.floating;
        success("1.0");
        success("1234.1234");
        successExpect("5.", new FloatLiteralNode(null, 5.0));
        successExpect("5.0", new FloatLiteralNode(null, 5.0));
        failure("5");
        successExpect("1.0", new FloatLiteralNode(null, 1.0));
        successExpect("1.23e-10", new FloatLiteralNode(null,1.23e-10));
        successExpect("1.23e+10", new FloatLiteralNode(null, 1.23e+10));
        successExpect("1.23E-10", new FloatLiteralNode(null, 1.23e-10));
        successExpect("1.0", new FloatLiteralNode(null, 1.0));
        success("-1.1234");
        success("- 1.1234");
        failure("- 1. 1234");
        failure("- 1 .1234");
        failure("- 1.23 e10");
        failure("- 1.23e 10");
        failure("a");
        failure("-a");
        successExpect("-1.1234", new FloatLiteralNode(null, -1.1234));
        successExpect("- 1.1234", new FloatLiteralNode(null,-1.1234));
    }

    @Test
    public void testStrings() {
        this.rule = grammar.string;
        successExpect("\"abc\"", new StringLiteralNode(null,"abc"));
        successExpect("\" a b c \"", new StringLiteralNode(null," a b c "));
        successExpect("\"\"", new StringLiteralNode(null,""));
        failure("\"\"\"");
//        success("\"\\\"\""); // TODO implement
//        failure("\"a\nb\""); // TODO implement
    }

    @Test
    public void testBool() {
        this.rule = grammar.reserved_lit;
        success("true");
        success("false");
        success("null");
        failure("True");
        failure("False");
        failure("truex");
        failure("falsex");
        failure("Null");
        failure("nullx");
        successExpect("true", new BoolLiteralNode(null,true));
        successExpect("false", new BoolLiteralNode(null,false));
        successExpect("null", new NullLiteralNode(null));
    }

    @Test
    public void testBasicExpression() {
        this.rule = grammar.prefix_expression;
        success("1");
        success("-1");
        success("1.0");
        success("-1.0");
        successExpect("- 1", new UnaryExpressionNode(null, NEG, new IntLiteralNode(null,1)));
        success("a");
        success("-a");
        successExpect("- a", new UnaryExpressionNode(null,NEG, new ReferenceNode(null,"a")));
        success("true");
        success("!true");
        successExpect("! true", new UnaryExpressionNode(null,NOT, new BoolLiteralNode(null,true)));
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
    public void testAssignment(){
        this.rule = grammar.assignment_stmt;
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
        successExpect("a = 1", new AssignmentNode(null, new ReferenceNode(null,"a"), new IntLiteralNode(null,1)));
        successExpect("a = true", new AssignmentNode(null,new ReferenceNode(null,"a"), new BoolLiteralNode(null,true)));

        success("a = 1");
        success("a = 1.0");
        success("a = true");
        success("a = null");
        success("a = makeArray()");
        success("a = makeDict()");
    }

    @Test
    public void testSimpleAddition() {
        this.rule = grammar.add_expr;
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
        successExpect("1 + 2", new BinaryExpressionNode(null,new IntLiteralNode(null,1), ADD, new IntLiteralNode(null,2)));
        successExpect("1 - 2", new BinaryExpressionNode(null,new IntLiteralNode(null,1), SUBTRACT, new IntLiteralNode(null,2)));
    }

    @Test
    public void testSimpleMultiplication() {
        this.rule = grammar.mult_expr;
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
        successExpect("1 * 2", new BinaryExpressionNode(null,new IntLiteralNode(null,1), MULTIPLY, new IntLiteralNode(null,2)));
        successExpect("1 / 2", new BinaryExpressionNode(null,new IntLiteralNode(null,1), DIVIDE, new IntLiteralNode(null,2)));
        successExpect("6 % 2", new BinaryExpressionNode(null,new IntLiteralNode(null,6), REMAINDER, new IntLiteralNode(null,2)));
    }

    @Test
    public void testSimpleMixedOperations() {
        this.rule = grammar.add_expr;
        success("1 + 2 + 3 - 4");
        success("1 + 2 * 3 / 4 + 5");
        success("1 - 2 + 3");
        success("-1 + 2");
        success("1 / 2+ 3");
        success("1 + 2 % 3");
        successExpect("1 + 2 % 3 * 4",
                new BinaryExpressionNode(
                        null,
                        new IntLiteralNode(null,1),
                        ADD,
                        new BinaryExpressionNode(
                                null,
                                new BinaryExpressionNode(
                                        null,
                                        new IntLiteralNode(null,2),
                                        REMAINDER,
                                        new IntLiteralNode(null,3)),
                                MULTIPLY,
                                new IntLiteralNode(null,4))));
        successExpect("1 + 2 % 3 + 4",
                new BinaryExpressionNode(
                        null,
                        new BinaryExpressionNode(
                                null,
                                new IntLiteralNode(null,1),
                                ADD,
                                new BinaryExpressionNode(
                                        null,
                                        new IntLiteralNode(null,2),
                                        REMAINDER,
                                        new IntLiteralNode(null,3))),
                        ADD,
                        new IntLiteralNode(null,4)));
    }

    @Test
    public void testAdvancedVarDef() {
        this.rule = grammar.assignment_stmt;
//        failure("a = true * 5 + \"coucou\""); // TODO
        successExpect("a = null", new AssignmentNode(null,new ReferenceNode(null,"a"), new NullLiteralNode(null)));
        successExpect("a = 1 + 2 * 3", new AssignmentNode(null,new ReferenceNode(null,"a"), new BinaryExpressionNode(null,new IntLiteralNode(null,1), ADD, new BinaryExpressionNode(null,new IntLiteralNode(null,2), MULTIPLY, new IntLiteralNode(null,3)))));
        successExpect("a = false || true && true", new AssignmentNode(null,new ReferenceNode(null,"a"), new BinaryExpressionNode(null,new BoolLiteralNode(null,false), OR, new BinaryExpressionNode(null,new BoolLiteralNode(null,true), AND, new BoolLiteralNode(null,true)))));
        successExpect("a = \"b\"+\"c\" - \"a\"",
                new AssignmentNode(null,
                        new ReferenceNode(null, "a"),
                        new BinaryExpressionNode(null,
                                new BinaryExpressionNode(null,
                                        new StringLiteralNode(null, "b"),
                                        ADD,
                                        new StringLiteralNode(null, "c")),
                                SUBTRACT,
                                new StringLiteralNode(null, "a"))));
        successExpect("a[0] = 1",
                new AssignmentNode(null,
                        new ArrayAccessNode(null,
                                new ReferenceNode(null, "a"),
                                new ArrayLiteralNode(null, Arrays.asList(new IntLiteralNode(null, 0)))),
                        new IntLiteralNode(null, 1)));
    }

    @Test
    public void testExpression() {
        this.rule = grammar.expression;
        success("1");
        success("a");
        success("true");
        success("\"x\"");

        success("a[i]");
        failure("a[]");
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

        successExpect("a[i]", new ArrayAccessNode(null, new ReferenceNode(null, "a"), new ReferenceNode(null,"i")));
        successExpect("a[1 + 2]", new ArrayAccessNode(null, new ReferenceNode(null, "a"), new BinaryExpressionNode(null, new IntLiteralNode(null,1), ADD, new IntLiteralNode(null,2))));
        successExpect("1 + 2 <= 3 * 4", new BinaryExpressionNode(null, new BinaryExpressionNode(null, new IntLiteralNode(null,1), ADD, new IntLiteralNode(null,2)), LOWER_EQUAL, new BinaryExpressionNode(null, new IntLiteralNode(null, 3), MULTIPLY, new IntLiteralNode(null,4))));
        successExpect("false || 1 + 2 * 3 <= 4 && true",
                new BinaryExpressionNode(null,
                        new BoolLiteralNode(null,false),
                        OR,
                        new BinaryExpressionNode(null,
                                new BinaryExpressionNode(null,
                                        new BinaryExpressionNode(null,
                                                new IntLiteralNode(null,1),
                                                ADD,
                                                new BinaryExpressionNode(null,
                                                        new IntLiteralNode(null,2),
                                                        MULTIPLY,
                                                        new IntLiteralNode(null,3))),
                                        LOWER_EQUAL,
                                        new IntLiteralNode(null,4)),
                                AND ,
                                new BoolLiteralNode(null,true))));
    }

    @Test
    public void testConditions() {
        this.rule = grammar.if_stmt;
        failure("if 1 == 2  a=3 ");
        success("if 1 == 2 { a=3 }");
        successExpect("if 1 == 2 { a=3 }",
                new IfNode(null,
                        new BinaryExpressionNode(null,new IntLiteralNode(null,1), EQUALITY, new IntLiteralNode(null,2)),
                        new BlockNode(null, Arrays.asList(new AssignmentNode(null,new ReferenceNode(null,"a"), new IntLiteralNode(null,3)))),
                        null));
        successExpect("if 1 == 2 { print(a) }",
                new IfNode(null,
                        new BinaryExpressionNode(null,new IntLiteralNode(null,1), EQUALITY, new IntLiteralNode(null,2)),
                        new BlockNode(null, Arrays.asList(new ExpressionStatementNode(null, new FunCallNode(null, new ReferenceNode(null,"print"), Arrays.asList(new ReferenceNode(null,"a")))))),
                        null));
        successExpect("if true { a=2 } else { a=3 }",
                new IfNode(null,
                        new BoolLiteralNode(null,true),
                        new BlockNode(null, Arrays.asList(new AssignmentNode(null,new ReferenceNode(null,"a"), new IntLiteralNode(null,2)))),
                        new BlockNode(null, Arrays.asList(new AssignmentNode(null,new ReferenceNode(null,"a"), new IntLiteralNode(null,3))))));
        successExpect("if a == 1 { a=2 } else if a { a=3 }",
                new IfNode(null,
                        new BinaryExpressionNode(null,new ReferenceNode(null,"a"), EQUALITY, new IntLiteralNode(null,1)),
                        new BlockNode(null, Arrays.asList(new AssignmentNode(null,new ReferenceNode(null,"a"), new IntLiteralNode(null,2)))),
                        new IfNode(null,
                                new ReferenceNode(null,"a"),
                                new BlockNode(null, Arrays.asList(new AssignmentNode(null, new ReferenceNode(null,"a"), new IntLiteralNode(null,3)))),
                                null)));
        success("if a == 1 { a=2 } else if a == true { a=3 } else if a == \"hello\" { a=\"world\" }");
        successExpect("if a == 1 { a=2 } else if a == true { a=3 } else if a == \"hello\" { a=\"world\" } else { a = null }",
                new IfNode(null,
                        new BinaryExpressionNode(null,new ReferenceNode(null,"a"), EQUALITY, new IntLiteralNode(null,1)),
                        new BlockNode(null, Arrays.asList(new AssignmentNode(null,new ReferenceNode(null,"a"), new IntLiteralNode(null,2)))),
                        new IfNode(null,
                                new BinaryExpressionNode(null,new ReferenceNode(null,"a"), EQUALITY, new BoolLiteralNode(null,true)),
                                new BlockNode(null, Arrays.asList(new AssignmentNode(null,new ReferenceNode(null,"a"), new IntLiteralNode(null,3)))),
                                new IfNode(null,
                                        new BinaryExpressionNode(null,new ReferenceNode(null,"a"), EQUALITY, new StringLiteralNode(null,"hello")),
                                        new BlockNode(null, Arrays.asList(new AssignmentNode(null, new ReferenceNode(null,"a"), new StringLiteralNode(null,"world")))),
                                        new BlockNode(null, Arrays.asList(new AssignmentNode(null, new ReferenceNode(null,"a"), new NullLiteralNode(null))))))));
        success("if a == 1 { a=2 b=2 } else if a == true { a=3 } else if a == \"hello\" { a=\"world\" }");
        failure("if a == 1 a=2 b=2 else if a == true { a=3 } else if a == \"hello\" { a=\"world\" }");
        failure("if { a == 1 } { a=2 } else if a == true { a=3 } else if a == \"hello\" { a=\"world\" }");
    }

    @Test
    public void testWhileLoops() {
        this.rule = grammar.while_stmt;
        successExpect("while 1 > 2 { a = b }",
                new WhileNode(null,
                        new BinaryExpressionNode(null,new IntLiteralNode(null,1), GREATER, new IntLiteralNode(null,2)),
                        new BlockNode(null, Arrays.asList(new AssignmentNode(null,new ReferenceNode(null,"a"), new ReferenceNode(null,"b"))))));
        successExpect("while true { a = b + 1 }",
                new WhileNode(null,
                        new BoolLiteralNode(null,true),
                        new BlockNode(null, Arrays.asList(
                                new AssignmentNode(null,
                                        new ReferenceNode(null,"a"),
                                        new BinaryExpressionNode(null,
                                                new ReferenceNode(null,"b"),
                                                ADD,
                                                new IntLiteralNode(null,1)))))));
        successExpect("while 1!=2 {a = 1}",
                new WhileNode(null,
                        new BinaryExpressionNode(null,new IntLiteralNode(null,1), NOT_EQUALS, new IntLiteralNode(null,2)),
                        new BlockNode(null, Arrays.asList(new AssignmentNode(null,new ReferenceNode(null,"a"), new IntLiteralNode(null,1))))));
        successExpect("while a == b { if c == 1 {b = 2}}",
                new WhileNode(null,
                        new BinaryExpressionNode(null,new ReferenceNode(null,"a"), EQUALITY, new ReferenceNode(null,"b")),
                        new BlockNode(null, Arrays.asList(
                                new IfNode(null,
                                    new BinaryExpressionNode(null,
                                            new ReferenceNode(null,"c"),
                                            EQUALITY,
                                            new IntLiteralNode(null,1)),
                                    new BlockNode(null, Arrays.asList(
                                            new AssignmentNode(null,
                                                    new ReferenceNode(null,"b"),
                                                    new IntLiteralNode(null,2)))),
                                    null)))));
        successExpect("while true { a = foo(a, b)}",
                new WhileNode(
                        null,
                        new BoolLiteralNode(null,true),
                        new BlockNode(null, Arrays.asList(
                                new AssignmentNode(null,
                                    new ReferenceNode(null,"a"),
                                    new FunCallNode(null,
                                            new ReferenceNode(null,"foo"),
                                            Arrays.asList(new ReferenceNode(null,"a"), new ReferenceNode(null,"b"))))))));
    }

    @Test
    public void testFunctions2() {
        this.rule = grammar.fun_decl;
        successExpect("fun bar() { a = 1 return a }",
                new FunDeclarationNode(null,
                        "bar",
                        Arrays.asList(),
                        new BlockNode(null, Arrays.asList(new AssignmentNode(null,new ReferenceNode(null,"a"), new IntLiteralNode(null,1)), new ReturnNode(null, new ReferenceNode(null,"a"))))));
        successExpect("fun bar() { return 1 }",
                new FunDeclarationNode(null,
                        "bar",
                        Arrays.asList(),
                        new BlockNode(null, Arrays.asList(new ReturnNode(null,new IntLiteralNode(null,1))))));
        successExpect("fun foo(a, b) { c = a + b \n d = c * c \n return d }",
                new FunDeclarationNode(null,
                        "foo",
                        Arrays.asList(
                                new ParameterNode(null,"a"),
                                new ParameterNode(null,"b")),
                        new BlockNode(null, Arrays.asList(
                                new AssignmentNode(null,new ReferenceNode(null,"c"), new BinaryExpressionNode(null,new ReferenceNode(null,"a"), ADD, new ReferenceNode(null,"b"))),
                                new AssignmentNode(null,new ReferenceNode(null,"d"), new BinaryExpressionNode(null,new ReferenceNode(null,"c"), MULTIPLY, new ReferenceNode(null,"c"))),
                                new ReturnNode(null,new ReferenceNode(null,"d"))))));
    }

    @Test
    public void testArrayAccess() {
        this.rule = grammar.suffix_expression;
        successExpect("a[i]",
                new ArrayAccessNode(null,
                        new ReferenceNode(null,"a"),
                        new ReferenceNode(null,"i")));
        successExpect("a[0]",
                new ArrayAccessNode(null,
                        new ReferenceNode(null,"a"),
                        new IntLiteralNode(null,0)));
        successExpect("a[1+2]",
                new ArrayAccessNode(null,
                    new ReferenceNode(null,"a"),
                    new BinaryExpressionNode(null,
                            new IntLiteralNode(null,1),
                            ADD,
                            new IntLiteralNode(null,2))));
    }

    @Test
    public void testFunctionCalls() {
        this.rule = grammar.suffix_expression;
        successExpect("foo(a, b)",
                new FunCallNode(null,
                        new ReferenceNode(null,"foo"),
                        Arrays.asList(new ReferenceNode(null,"a"), new ReferenceNode(null,"b"))));
        successExpect("foo(1, b)",
                new FunCallNode(null,
                        new ReferenceNode(null,"foo"),
                        Arrays.asList(new IntLiteralNode(null,1), new ReferenceNode(null,"b"))));
        successExpect("foo(b)",
                new FunCallNode(null,
                        new ReferenceNode(null,"foo"),
                        Arrays.asList( new ReferenceNode(null,"b"))));
        successExpect("foo()",
                new FunCallNode(null,
                        new ReferenceNode(null,"foo"),
                        Arrays.asList()));
    }

    @Test
    public void testClass2() {
        this.rule = grammar.klass;
        successExpect("class Foo {}",
                new ClassNode(null,
                        "Foo",
                        Arrays.asList()));
        successExpect("class Foo { fun bar() { return 1 } }",
                new ClassNode(null,
                        "Foo",
                        Arrays.asList(
                                new FunDeclarationNode(null,
                                        "bar",
                                        Arrays.asList(),
                                        new BlockNode(null, Arrays.asList(new ReturnNode(null, new IntLiteralNode(null,1))))))));
    }

    @Test
    public void testArgs() {
        this.rule = grammar.root;
        // TODO check args
    }

    @Test
    public void testClassAsRoot() {
        this.rule = grammar.root;
        failure("a = 1 + 2");
        failure("fun bar() { return 1 }");
        success("class Foo { fun bar(a) { a[0]=1 } }");
        successExpect("class Foo {}",
                new ClassNode(null,"Foo", Arrays.asList()));
        successExpect("class Foo { fun bar() { return 1 } }",
                new ClassNode(null,
                        "Foo",
                        Arrays.asList(
                                new FunDeclarationNode(null,
                                        "bar",
                                        Arrays.asList(),
                                        new BlockNode(null, Arrays.asList(new ReturnNode(null,new IntLiteralNode(null,1))))))));
    }
}

