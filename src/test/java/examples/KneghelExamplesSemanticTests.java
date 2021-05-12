package examples;

import ast.*;
import norswap.autumn.AutumnTestFixture;
import norswap.autumn.positions.LineMapString;
import norswap.uranium.Reactor;
import norswap.uranium.UraniumTestFixture;
import norswap.utils.visitors.Walker;
import grammar.KneghelGrammar;
import semantic.SemanticAnalysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class KneghelExamplesSemanticTests extends UraniumTestFixture {

    KneghelGrammar grammar = new KneghelGrammar();
    AutumnTestFixture autumnFixture = new AutumnTestFixture();

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
        return ast.toString() + " (" + ((KneghelNode) ast).span.startString(map) + ")";
    }

    @Override
    protected void configureSemanticAnalysis(Reactor reactor, Object ast) {
        Walker<KneghelNode> walker = SemanticAnalysis.createWalker(reactor);
        walker.walk(((KneghelNode) ast));
    }


    public static void main(String[] args) {
        String[] files = new String[]{"Fibonacci.kneghel", "FizzBuzz.kneghel", "Prime.kneghel", "Sort.kneghel", "Uniq.kneghel"};

        KneghelExamplesSemanticTests test = new KneghelExamplesSemanticTests();

        for (int i = 0; i < files.length; i++) {
            String content = "";
            try {
                Path file = Path.of("src/examples/" + files[i]);
                content = Files.readString(file);
            } catch (IOException e) {
                System.err.println("Error reading file.");
            }

            System.out.println("Testing " + (i + 1) + "/" + files.length + " : " + files[i]);
            try {
                test.successInput(content);
                System.out.println("Semantic test succeeded.\n");
            } catch (Error e) {
                System.out.println(e);
            }
        }
    }
}