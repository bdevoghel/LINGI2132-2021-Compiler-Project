package AST;

import norswap.autumn.positions.Span;

public class IntegerNode extends ExpressionNode {
    public int value;

    public IntegerNode(Span span, int value) {
        super(span);
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        IntegerNode other = (IntegerNode) obj;
        return this.value == other.value;
    }

    @Override
    public String toString() {
        return "IntegerNode:" + value;
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
