package AST;

public class ParsingNode implements ExpressionNode{
    private ExpressionNode value;

    public ParsingNode(ExpressionNode toParse){
        this.value = toParse;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        ParsingNode other = (ParsingNode) obj;
        return this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "ParsingNode:" + value;
    }
}
