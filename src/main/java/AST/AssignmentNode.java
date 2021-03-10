package AST;

public class AssignmentNode implements StatementNode {
    private IdentifierNode variable;
    private ExpressionNode value;

    public AssignmentNode(IdentifierNode variable, ExpressionNode value) {
        this.variable = variable;
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        AssignmentNode other = (AssignmentNode) obj;
        return
                (this.value == null && other.value == null || this.value.equals(other.value)) &&
                this.variable.equals(other.variable);
    }

    @Override
    public String toString() {
        return "AssignmentNode:[" + variable + "=" + value + "]";
    }
}
