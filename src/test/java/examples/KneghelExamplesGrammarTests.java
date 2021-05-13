package examples;

import ast.*;
import static ast.BinaryOperator.*;

import norswap.autumn.ParseResult;
import norswap.autumn.AutumnTestFixture;
import grammar.KneghelGrammar;

import java.util.Arrays;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class KneghelExamplesGrammarTests extends AutumnTestFixture {

    KneghelGrammar grammar = new KneghelGrammar();

    public static void main(String[] args) {
        String[] files = new String[] {"Fibonacci.kneghel", "FizzBuzz.kneghel", "Prime.kneghel", "Sort.kneghel", "Uniq.kneghel"};

        KneghelExamplesGrammarTests test = new KneghelExamplesGrammarTests();
        test.rule = test.grammar.root;

        for (int i = 0; i < files.length; i++) {
            String content = "";
            try {
                Path file = Path.of("src/examples/" + files[i]);
                content = Files.readString(file);
            } catch (IOException e) {
                System.err.println("Error reading file.");
            }

            System.out.println("Testing " + (i+1)+"/"+files.length + " : " + files[i]);
            ParseResult result = switch (files[i]) {
                case "Fibonacci.kneghel" -> test.testFibonacci(content);
                case "FizzBuzz.kneghel" -> test.testFizzBuzz(content);
                case "Prime.kneghel" -> test.testPrime(content);
                case "Sort.kneghel" -> test.testSort(content);
                case "Uniq.kneghel" -> test.testUniq(content);
                default -> null;
            };
            System.out.println(result);
        }
    }

    public ParseResult testPrime(String testString) {
        Object expectedValue =
                new ClassNode(null,
                        "Prime",
                        Arrays.asList(
                                new FunDeclarationNode(null,
                                        "isPrime",
                                        Arrays.asList(new ParameterNode(null, "number")),
                                        new BlockNode(null, Arrays.asList(
                                                new IfNode(null,
                                                        new BinaryExpressionNode(null, new ReferenceNode(null, "number"), LOWER_EQUAL, new IntLiteralNode(null, 1)),
                                                        new BlockNode(null, Arrays.asList(new ReturnNode(null, new BoolLiteralNode(null, false)))),
                                                        null
                                                ),
                                                new AssignmentNode(null, new ReferenceNode(null, "prime"), new BoolLiteralNode(null, true)),
                                                new AssignmentNode(null, new ReferenceNode(null, "i"), new IntLiteralNode(null, 2)),
                                                new WhileNode(null,
                                                        new BinaryExpressionNode(null,
                                                                new BinaryExpressionNode(null, new ReferenceNode(null, "i"), LOWER, new ReferenceNode(null, "number")),
                                                                AND,
                                                                new ReferenceNode(null, "prime")),
                                                        new BlockNode(null, Arrays.asList(
                                                                new IfNode(null,
                                                                        new BinaryExpressionNode(null,
                                                                                new BinaryExpressionNode(null, new ReferenceNode(null, "number"), REMAINDER, new ReferenceNode(null, "i")),
                                                                                EQUALITY,
                                                                                new IntLiteralNode(null, 0)),
                                                                        new BlockNode(null, Arrays.asList(new AssignmentNode(null, new ReferenceNode(null, "prime"), new BoolLiteralNode(null, false)))),
                                                                        null),
                                                                new AssignmentNode(null, new ReferenceNode(null, "i"), new BinaryExpressionNode(null, new ReferenceNode(null, "i"), ADD, new IntLiteralNode(null, 1)))))
                                                ),
                                                new ReturnNode(null, new ReferenceNode(null, "prime"))
                                        ))
                                ),
                                new FunDeclarationNode(null,
                                        "main",
                                        Arrays.asList(),
                                        new BlockNode(null, Arrays.asList(
                                                new AssignmentNode(null,
                                                        new ReferenceNode(null, "N"),
                                                        new FunCallNode(null,
                                                                new ReferenceNode(null, "int"),
                                                                Arrays.asList(
                                                                        new ArrayAccessNode(null, new ReferenceNode(null, "args"), new IntLiteralNode(null, 0))))),
                                                new AssignmentNode(null, new ReferenceNode(null, "current"), new IntLiteralNode(null, 2)),
                                                new AssignmentNode(null, new ReferenceNode(null, "count"), new IntLiteralNode(null, 0)),
                                                new WhileNode(null,
                                                        new BinaryExpressionNode(null, new ReferenceNode(null, "count"), LOWER, new ReferenceNode(null, "N")),
                                                        new BlockNode(null, Arrays.asList(
                                                                new IfNode(null,
                                                                        new FunCallNode(null, new ReferenceNode(null, "isPrime"), Arrays.asList(new ReferenceNode(null, "current"))),
                                                                        new BlockNode(null, Arrays.asList(
                                                                                new ExpressionStatementNode(null, new FunCallNode(null, new ReferenceNode(null, "print"), Arrays.asList(new ReferenceNode(null, "current")))),
                                                                                new AssignmentNode(null, new ReferenceNode(null, "count"), new BinaryExpressionNode(null, new ReferenceNode(null, "count"), ADD, new IntLiteralNode(null, 1))))),
                                                                        null),
                                                                new AssignmentNode(null, new ReferenceNode(null, "current"), new BinaryExpressionNode(null, new ReferenceNode(null, "current"), ADD, new IntLiteralNode(null, 1)))))
                                                )
                                        ))
                                )
                        )
                );
        return successExpect(testString, expectedValue);
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
