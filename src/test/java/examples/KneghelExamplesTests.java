package examples;

import AST.*;
import static AST.BinaryOperator.*;

import norswap.autumn.ParseResult;
import norswap.autumn.AutumnTestFixture;
import parser.KneghelGrammar;

import java.util.Arrays;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class KneghelExamplesTests extends AutumnTestFixture {

    KneghelGrammar parser = new KneghelGrammar();

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
                new ClassStatementNode(null,
                        Arrays.asList(
                                new FunctionStatementNode(null,
                                        new IdentifierNode(null, "isPrime"),
                                        new FunctionArgumentsNode(null, Arrays.asList(new IdentifierNode(null, "number"))),
                                        Arrays.asList(
                                                new IfStatementNode(null,
                                                        new BinaryExpressionNode(null, new IdentifierNode(null, "number"), LESS_OR_EQUAL, new IntegerNode(null, 1)),
                                                        Arrays.asList(new ReturnStatementNode(null, new BooleanNode(null, false)))
                                                ),
                                                new AssignmentNode(null, new IdentifierNode(null, "prime"), new BooleanNode(null, true)),
                                                new AssignmentNode(null, new IdentifierNode(null, "i"), new IntegerNode(null, 2)),
                                                new WhileStatementNode(null,
                                                        new BinaryExpressionNode(null,
                                                                new BinaryExpressionNode(null, new IdentifierNode(null, "i"), LESS_THAN, new IdentifierNode(null, "number")),
                                                                AND,
                                                                new IdentifierNode(null, "prime")),
                                                        Arrays.asList(
                                                                new IfStatementNode(null,
                                                                    new BinaryExpressionNode(null,
                                                                            new BinaryExpressionNode(null, new IdentifierNode(null, "number"), MODULO, new IdentifierNode(null, "i")),
                                                                            EQUAL,
                                                                            new IntegerNode(null, 0)),
                                                                    Arrays.asList(new AssignmentNode(null, new IdentifierNode(null, "prime"), new BooleanNode(null, false)))
                                                                ),
                                                                new AssignmentNode(null, new IdentifierNode(null, "i"), new BinaryExpressionNode(null, new IdentifierNode(null, "i"), ADD, new IntegerNode(null, 1))))
                                                ),
                                                new ReturnStatementNode(null, new IdentifierNode(null, "prime")))
                                ),
                                new FunctionStatementNode(null,
                                        new IdentifierNode(null, "main"),
                                        new FunctionArgumentsNode(null, Arrays.asList(new IdentifierNode(null, "args"))),
                                        Arrays.asList(
                                                new AssignmentNode(null,
                                                        new IdentifierNode(null, "N"),
                                                        new FunctionCallNode(null,
                                                                new IdentifierNode(null, "int"),
                                                                new FunctionArgumentsNode(null,
                                                                        Arrays.asList(new ArrayMapAccessNode(null, new IdentifierNode(null, "args"), new IntegerNode(null, 0)))))
                                                ),
                                                new AssignmentNode(null, new IdentifierNode(null, "current"), new IntegerNode(null, 2)),
                                                new AssignmentNode(null, new IdentifierNode(null, "count"), new IntegerNode(null, 0)),
                                                new WhileStatementNode(null,
                                                        new BinaryExpressionNode(null, new IdentifierNode(null, "count"), LESS_THAN, new IdentifierNode(null, "N")),
                                                        Arrays.asList(
                                                                new IfStatementNode(null,
                                                                        new FunctionCallNode(null, new IdentifierNode(null, "isPrime"), new FunctionArgumentsNode(null, Arrays.asList(new IdentifierNode(null, "current")))),
                                                                        Arrays.asList(
                                                                                new AssignmentNode(null, new IdentifierNode(null, "_"), new FunctionCallNode(null, new IdentifierNode(null, "print"), new FunctionArgumentsNode(null, Arrays.asList(new IdentifierNode(null, "current"))))),
                                                                                new AssignmentNode(null, new IdentifierNode(null, "count"), new BinaryExpressionNode(null, new IdentifierNode(null, "count"), ADD, new IntegerNode(null, 1))))
                                                                ),
                                                                new AssignmentNode(null, new IdentifierNode(null, "current"), new BinaryExpressionNode(null, new IdentifierNode(null, "current"), ADD, new IntegerNode(null, 1))))
                                                ),
                                                new ReturnStatementNode(null, new IntegerNode(null, 0))
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
