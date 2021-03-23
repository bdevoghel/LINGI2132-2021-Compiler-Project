package AST;

import norswap.autumn.positions.Span;

public class FunctionCallNode extends ExpressionNode {
    private IdentifierNode function;
    private FunctionArgumentsNode arguments;

    public FunctionCallNode(Span span, IdentifierNode function, FunctionArgumentsNode arguments) {
        super(span);
        this.function = function;
        this.arguments = arguments;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        FunctionCallNode other = (FunctionCallNode) obj;
        return this.function.equals(other.function) && this.arguments.equals(other.arguments);
    }

    @Override
    public String toString() {
        return "FunctionCallNode:" + function + "(" + arguments + ")";
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
