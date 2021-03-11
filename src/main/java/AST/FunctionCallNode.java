package AST;

public class FunctionCallNode implements ExpressionNode {
    private IdentifierNode function;
    private FunctionArgumentsNode arguments;

    public FunctionCallNode(IdentifierNode function, FunctionArgumentsNode arguments) {
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
}
