package AST;

import java.util.List;

public class FunctionArgumentsNode implements ExpressionNode {
    private List arguments;

    public FunctionArgumentsNode(List arguments) {
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
}
