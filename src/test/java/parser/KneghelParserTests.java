package parser;

import AST.*;
import org.testng.annotations.Test;
import norswap.autumn.TestFixture;

import static AST.BinaryOperator.*;

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
    public void testStrings() {
        this.rule = parser.string;
        success("\"abc\"");
        success("\"a b c\"");
        success("\"\"");
        failure("\"\"\"");
//        success("\"\\\"\""); // TODO implement
//        failure("\"a\nb\""); // TODO implement
        successExpect("\"abc\"", new StringNode("\"abc\""));
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
        this.rule = parser.value;
        success("1");
        success("-1");
        success("a");
        success("-a");
        success("true");
        success("!true");
        success("\"a\"");
        // TODO complete with negative variables etc etc
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
        successExpect("a = 1", new VariableDefinitionNode(new IdentifierNode("a"), new IntegerNode(1)));
        successExpect("a = true", new VariableDefinitionNode(new IdentifierNode("a"), new BooleanNode(true)));
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
        successExpect("a = 1 + 2 * 3", new VariableDefinitionNode(new IdentifierNode("a"), new BinaryExpressionNode(new IntegerNode(1), ADD, new BinaryExpressionNode(new IntegerNode(2), MULTIPLY, new IntegerNode(3)))));
        successExpect("a = false || true && true", new VariableDefinitionNode(new IdentifierNode("a"), new BinaryExpressionNode(new BooleanNode(false), OR, new BinaryExpressionNode(new BooleanNode(true), AND, new BooleanNode(true)))));
    }

    @Test
    public void testLogicExpression() {
        this.rule = parser.logicExpression;
        success("1 == 2");
        success("1 != 2");
        success("1 > 2");
        success("1 < 2");
        success("1 >= 2");
        success("1 <= 2");
    }

    @Test
    public void testConditions() {
        this.rule = parser.ifStatement;
        success("if 1 == 2 { a=2 }");
    }
}
