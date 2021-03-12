package AST;

public class ParsingNode implements ExpressionNode{
    private int value;

    public ParsingNode(ExpressionNode toParse){
        IdentifierNode test = new IdentifierNode(null);
        StringNode test2 = new StringNode(null);
        if (toParse.getClass() == test.getClass()){
            IdentifierNode toParseId = (IdentifierNode) toParse;
            this.value = Integer.parseInt(toParseId.getValue());
        }
        else if (toParse.getClass() == test2.getClass()){
            StringNode toParseSt = (StringNode) toParse;
            this.value = Integer.parseInt(toParseSt.getValue());
        }
        new IntegerNode(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        ParsingNode other = (ParsingNode) obj;
        return this.value==other.value;
    }

    @Override
    public String toString() {
        return "ParsingNode:" + value;
    }
}
