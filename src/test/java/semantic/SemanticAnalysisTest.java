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
    public void testInteger(){
        autumnFixture.rule = grammar.integer;
        successInput("5");
        successInput("5655"); //TODO failure input
        successInput("-1");
    }

    @Test
    public void testDouble(){
        autumnFixture.rule = grammar.doub;
        successInput("5.");
        successInput("5.0");
        successInput("-5478.32"); //TODO failure input
    }

    @Test
    public void testBool(){
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
        //failureInput("1 + a"); //TODO JE NE SAIS PAS COMMENT FAIRE DES FAILURES
    }

    @Test
    public void testUnaryExpression() {
        autumnFixture.rule = grammar.prefixExpression;
        successInput("-1");
        successInput("!true");
        successInput("- 2.0");
    }


    /*@Test
    public void testIfStatement(){
        successInput("if (true == false) 1 + 1 + 10000");
        successInput("if (true) 1 + 1 + 10000");
        failureInput("if (1 + 1) 1 + 1 + 10000");
        failureInput("if (5) 1 + 1 + 10000");
    }*/
}
