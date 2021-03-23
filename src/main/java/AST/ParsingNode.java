package AST;

import norswap.autumn.positions.Span;

public class ParsingNode extends ExpressionNode{
    private ExpressionNode value;

    public ParsingNode(Span span, ExpressionNode toParse){
        super(span);
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

    /**
     * Returns a <b>brief</b> overview of the content of the node, suitable to be printed
     * in a single line.
     */
    @Override
    public String contents() {
        return this.toString();
    }
}
