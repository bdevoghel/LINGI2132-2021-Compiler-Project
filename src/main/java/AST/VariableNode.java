package AST;

public class VariableNode implements ASTNode {
    private String variableName;
    private boolean isNegative;

    public VariableNode(String variableName) {
        new VariableNode(variableName, false);
    }

    public VariableNode(String variableName, boolean isNegative) {
        this.variableName = variableName;
        this.isNegative = isNegative;
    }
}
