package AST;

import norswap.autumn.positions.Span;

import java.util.List;

public class FunctionArgumentsNode extends ExpressionNode {
    public List elements;

    public FunctionArgumentsNode(Span span, List elements) {
        super(span);
        this.elements = elements;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        FunctionArgumentsNode other = (FunctionArgumentsNode) obj;
        return this.elements.equals(other.elements);
    }

    @Override
    public String toString() {
        return "FunctionArgumentsNode:" + elements;
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
