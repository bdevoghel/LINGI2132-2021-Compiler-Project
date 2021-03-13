package examples;

import AST.*;
import norswap.autumn.TestFixture;
import org.testng.annotations.Test;
import parser.KneghelParser;

import java.util.Arrays;

import static AST.BinaryOperator.*;

public class KneghelExamplesTests extends TestFixture {

    KneghelParser parser = new KneghelParser();

    @Test
    public void testPrime() {
        this.rule = parser.root;
        successExpect("class Prime {\n" +
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
                "        N = int(args[0])\n" +
                "        current = 2\n" +
                "        count = 0\n" +
                "        while count < N {\n" +
                "            if isPrime(current) {\n" +
                "                print(current)\n" +
                "                count = count + 1\n" +
                "            }\n" +
                "            current = current + 1\n" +
                "        }\n" +
                "    }\n" +
                "}",
                new ClassStatementNode(
                        Arrays.asList(
                                new FunctionStatementNode(
                                        new IdentifierNode("isPrime"),
                                        new FunctionArgumentsNode(Arrays.asList(new IdentifierNode("number"))),
                                        Arrays.asList(
                                                new IfStatementNode(
                                                        new BinaryExpressionNode(new IdentifierNode("number"), LESS_OR_EQUAL, new IntegerNode(1)),
                                                        Arrays.asList(new ReturnStatementNode(new BooleanNode(false)))
                                                ),
                                                new AssignmentNode(new IdentifierNode("prime"), new BooleanNode(true)),
                                                new AssignmentNode(new IdentifierNode("i"), new IntegerNode(2)),
                                                new WhileStatementNode(
                                                        new BinaryExpressionNode(
                                                                new BinaryExpressionNode(new IdentifierNode("i"), LESS_THAN, new IdentifierNode("number")),
                                                                AND,
                                                                new IdentifierNode("prime")),
                                                        Arrays.asList(
                                                                new IfStatementNode(
                                                                    new BinaryExpressionNode(
                                                                            new BinaryExpressionNode(new IdentifierNode("number"), MODULO, new IdentifierNode("i")),
                                                                            EQUAL,
                                                                            new IntegerNode(0)),
                                                                    Arrays.asList(new AssignmentNode(new IdentifierNode("prime"), new BooleanNode(false)))
                                                                ),
                                                                new AssignmentNode(new IdentifierNode("i"), new BinaryExpressionNode(new IdentifierNode("i"), ADD, new IntegerNode(1))))
                                                ),
                                                new ReturnStatementNode(new IdentifierNode("prime")))
                                ),
                                new FunctionStatementNode(
                                        new IdentifierNode("main"),
                                        new FunctionArgumentsNode(Arrays.asList(new IdentifierNode("args"))),
                                        Arrays.asList(
                                                new AssignmentNode(
                                                        new IdentifierNode("N"),
                                                        new FunctionCallNode(
                                                                new IdentifierNode("int"),
                                                                new FunctionArgumentsNode(
                                                                        Arrays.asList(new ArrayMapAccessNode(new IdentifierNode("args"), new IntegerNode(0)))))
                                                ),
                                                new AssignmentNode(new IdentifierNode("current"), new IntegerNode(2)),
                                                new AssignmentNode(new IdentifierNode("count"), new IntegerNode(0)),
                                                new WhileStatementNode(
                                                        new BinaryExpressionNode(new IdentifierNode("count"), LESS_THAN, new IdentifierNode("N")),
                                                        Arrays.asList(
                                                                new IfStatementNode(
                                                                        new FunctionCallNode(new IdentifierNode("isPrime"), new FunctionArgumentsNode(Arrays.asList(new IdentifierNode("current")))),
                                                                        Arrays.asList(
                                                                                new FunctionCallNode(new IdentifierNode("print"), new FunctionArgumentsNode(Arrays.asList(new IdentifierNode("current")))),
                                                                                new AssignmentNode(new IdentifierNode("count"), new BinaryExpressionNode(new IdentifierNode("count"), ADD, new IntegerNode(1))))
                                                                ),
                                                                new AssignmentNode(new IdentifierNode("current"), new BinaryExpressionNode(new IdentifierNode("current"), ADD, new IntegerNode(1))))
                                                ))
                                ))
                )
        );
    }
}
