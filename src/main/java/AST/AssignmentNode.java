package AST;

import norswap.autumn.positions.Span;

public class AssignmentNode extends DeclarationNode {
    public ExpressionNode variable;
    public ExpressionNode value;

    public AssignmentNode(Span span, ExpressionNode variable, ExpressionNode value) {
        super(span);
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

    @Override public String contents ()
    {
        String leftEqual = variable.contents() + " = ";

        String candidate = leftEqual + value.contents();
        if (candidate.length() <= contentsBudget())
            return candidate;

        candidate = leftEqual + "(?)";
        return candidate.length() <= contentsBudget()
                ? candidate
                : "(?) = (?)";
    }

    @Override
    public String toString() {
        return "AssignmentNode:[" + variable + "=" + value + "]";
    }

    @Override
    public String name() {
        return variable.toString();
    }

    @Override
    public String declaredThing() {
        return null;
    }
}
