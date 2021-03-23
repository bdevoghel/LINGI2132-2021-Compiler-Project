package AST;

import norswap.autumn.positions.Span;

import java.util.List;

public class FunctionArgumentsNode extends ExpressionNode {
    private List arguments;

    public FunctionArgumentsNode(Span span, List arguments) {
        super(span);
        this.arguments = arguments;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        FunctionArgumentsNode other = (FunctionArgumentsNode) obj;
        return this.arguments.equals(other.arguments);
    }

    @Override
    public String toString() {
        return "FunctionArgumentsNode:" + arguments;
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
