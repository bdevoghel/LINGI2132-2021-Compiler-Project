package parser;

import AST.IntegerNode;
import org.testng.annotations.Test;
import norswap.autumn.TestFixture;

public class KneghelParserTests extends TestFixture {

    KneghelParser parser = new KneghelParser();

    @Test
    public void testInteger() {
        this.rule = parser.integer;
        success("1");
        success("-5");
        success("- 5");
        success("1234");
        failure("ab");
        successExpect("1", new IntegerNode(1));
        successExpect("-1", new IntegerNode(-1));
        successExpect("- 1", new IntegerNode(-1));
    }

    @Test
    public void testValue() {
        this.rule = parser.value;
        success("1");
        success("-5");
        success("- 5");
        success("1234");
        success("ab");
        success("-cd");
        failure("12ab");
        success("true");
        success("True");
        success("false");
        success("False");
        success("false");
        success("tHiSALsowORKs");
    }

    @Test
    public void testAddition() {
        this.rule = parser.addition;
        success("1+2");
        success("1 + 2");
        success("-5+ 2");
        success("- 5 - 2");
        success("- 5 -2");
        failure("5 + + 2");
        failure("5 ++ 2");
        success("5 + - 2");
        failure("5 + ");
    }

    @Test
    public void testMultiplication(){
        this.rule = parser.multiplication;
        success("1 * 1 * 1");
        success("1*1*1");
        success("1*1/1");
        success("1*1");
        failure("5 * ");
        failure(" 5 ** 2");
        failure(" 5 / / 2");
        failure(" 5 */ 2");
    }

    @Test
    public void testMixedOperations() {
        this.rule = parser.addition;
        success("1 + 1 + 1000 -10");
        success("1 + 90 * 85 / 3 + 3");
        success("1 - 1 + 2");
        success("1 / 2+ 1");
        success("-1 + 1");
    }

    @Test
    public void testBooleans() {
        this.rule = parser.bool;
        success("true");
        success("false");
        failure("truefalse");
        failure("falsetrue");
        failure("True");
    }

    @Test
    public void testComment() {
        this.rule = parser.ws;
        success("// comment");
        success(" // comment");
        success("// comment\n");
        success("//comment\n");
        failure("/notcomment\n");
        failure("falsetrue\n");
        success("/* comment */");
        success("/* comment \n hey */");
        success("/* comment \n hey \n */");
        success("/*comment \n hey \n*/");
        failure("/* ");
        success("// comment \n // comment\n");
    }

//    @Test
//    public void testCommentAsRoot() {
//        this.rule = parser.root;
//        success("1 # comment\n");
//        success("1# comment\n");
//        success("1 #comment\n");
//        success("1 //comment \n 1+1");
//        failure("1 /notcomment\n");
//        success("1+1 /* comment */");
//        success("1+1 \n /* comment */ 1+1");
//        success("1+1 \n /* comment */ \n 1+1");
//        success("abc /* comment \n hey */ ab ");
//        failure("/* ");
//        success("1 // comment \n // comment\n");
//    }

    @Test
    public void testVarDef(){
        this.rule = parser.variableDefinition;
        success("a = 1");
        success("a=1");
        success("abc = 1");
        success("abc = a");
        success("a = 1 + 2");
        success("a = 1 / 2");
        failure("a = // hey");
    }
}
