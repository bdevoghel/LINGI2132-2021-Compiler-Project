package examples;

import AST.*;
import static AST.BinaryOperator.*;

import norswap.autumn.ParseResult;
import norswap.autumn.AutumnTestFixture;
import org.testng.annotations.Test;
import parser.KneghelParser;

import java.util.Arrays;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class KneghelExamplesTests extends AutumnTestFixture {

    KneghelParser parser = new KneghelParser();

    public static void main(String[] args) {
        String[] files = new String[] {"Fibonacci.kneghel", "FizzBuzz.kneghel", "Prime.kneghel", "Sort.kneghel", "Uniq.kneghel"};

        KneghelExamplesTests test = new KneghelExamplesTests();
        test.rule = test.parser.root;

        for (int i = 0; i < files.length; i++) {
            String content = "";
            try {
                Path file = Path.of("src/examples/" + files[i]);
                content = Files.readString(file);
            } catch (IOException e) {
                System.err.println("Error reading file.");
            }

            System.out.println("Testing " + (i+1)+"/"+files.length + " : " + files[i]);
            ParseResult result = null;
            if (files[i].equals("Fibonacci.kneghel")) {
                result = test.testFibonacci(content);
            } else if (files[i].equals("FizzBuzz.kneghel")) {
                result = test.testFizzBuzz(content);
            } else if (files[i].equals("Prime.kneghel")) {
                result = test.testPrime(content);
            } else if (files[i].equals("Sort.kneghel")) {
                result = test.testSort(content);
            } else if (files[i].equals("Uniq.kneghel")) {
                result = test.testUniq(content);
            }
            System.out.println(result);
        }
    }

    public ParseResult testPrime(String testString) {
        return successExpect(testString,
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
                                                                                new AssignmentNode(new IdentifierNode("_"), new FunctionCallNode(new IdentifierNode("print"), new FunctionArgumentsNode(Arrays.asList(new IdentifierNode("current"))))),
                                                                                new AssignmentNode(new IdentifierNode("count"), new BinaryExpressionNode(new IdentifierNode("count"), ADD, new IntegerNode(1))))
                                                                ),
                                                                new AssignmentNode(new IdentifierNode("current"), new BinaryExpressionNode(new IdentifierNode("current"), ADD, new IntegerNode(1))))
                                                ),
                                                new ReturnStatementNode(new IntegerNode(0))
                                                )
                                ))
                )
        );
    }

    public ParseResult testFibonacci(String testString) {
        return success(testString);
    }

    public ParseResult testFizzBuzz(String testString) {
        return success(testString);
    }

    public ParseResult testSort(String testString) {
        return success(testString);
    }

    public ParseResult testUniq(String testString) {
        return success(testString);
    }
}
