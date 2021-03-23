package AST;

import norswap.autumn.positions.Span;

public class IdentifierNode extends ExpressionNode {
    private String value;

    public IdentifierNode(Span span, String value) {
        super(span);
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        IdentifierNode other = (IdentifierNode) obj;
        return this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "IdentifierNode:" + value;
    }

    public String getValue(){
        return this.value;
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
