package examples;

import AST.*;
import norswap.autumn.TestFixture;
import org.testng.annotations.Test;
import parser.KneghelParser;

import java.util.Arrays;

import static AST.BinaryOperator.*;
import static AST.UnaryOperator.NEG;
import static AST.UnaryOperator.NOT;

public class KneghelExamplesTests extends TestFixture {

    KneghelParser parser = new KneghelParser();

    @Test
    public void testPrime() {
        this.rule = parser.functionStatement;
        success("fun isPrime(number) {\n" +
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
                "    }");
//        this.rule = parser.root;
//        success("class Prime {\n fun isPrime(number) {\n if number <= 1 {\n return false\n }\n prime = true\n i = 2\n while i < number && prime {\n if number % i == 0 {\n prime = false\n }\n i = i + 1\n }\n return prime\n }\n fun main(args) {\n N = int(args[0])\n current = 2\n count = 0\n while count < N {\n if isPrime(current) {\n println(current)\n count = count + 1\n }\n current = current + 1\n }\n }\n}");
    }
}
