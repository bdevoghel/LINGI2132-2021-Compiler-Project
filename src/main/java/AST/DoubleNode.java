package AST;

import norswap.autumn.positions.Span;

public class DoubleNode extends ExpressionNode {
    public double value;

    public DoubleNode(Span span, double value) {
        super(span);
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        DoubleNode other = (DoubleNode) obj;
        return this.value == other.value;
    }

    @Override
    public String toString() {
        return "DoubleNode:" + value;
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
