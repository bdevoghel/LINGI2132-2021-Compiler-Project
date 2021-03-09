package AST;

public class VariableDefinitionNode implements ASTNode {
    private IdentifierNode variable;
    private Object value;

    public VariableDefinitionNode(IdentifierNode variable, Object value) {
        this.variable = variable;
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        VariableDefinitionNode other = (VariableDefinitionNode) obj;
        return
                this.value.equals(other.value) &&
                this.variable.equals(other.variable);
    }

    @Override
    public String toString() {
        return "VariableDefinitionNode:" + variable.toString() + "=" + value;
    }
}
