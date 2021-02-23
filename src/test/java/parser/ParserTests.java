package parser;

import org.testng.annotations.Test;
import norswap.autumn.TestFixture;
import parser.Parser;

public class ParserTests extends TestFixture {

    Parser parser = new Parser();

    @Test
    public void testNone() {
        this.rule = parser.root;
        success("");
        failure("not ok");
    }
}
