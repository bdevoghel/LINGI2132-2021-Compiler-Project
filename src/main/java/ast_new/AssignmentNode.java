package ast_new;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public class AssignmentNode extends DeclarationNode
{
    public final ExpressionNode left;
    public final ExpressionNode right;

    public AssignmentNode (Span span, Object left, Object right) {
        super(span);
        this.left = Util.cast(left, ExpressionNode.class);
        this.right = Util.cast(right, ExpressionNode.class);
    }

    @Override
    public String name()
    {
        if (left instanceof ReferenceNode) {
            return ((ReferenceNode) left).name;
        } else if (left instanceof ArrayAccessNode && ((ArrayAccessNode) left).array instanceof ReferenceNode) {
            return ((ReferenceNode) ((ArrayAccessNode) left).array).name;
        } else {
            throw new RuntimeException("NOT YET IMPLEMENTED"); // TODO
        }
    }

    @Override
    public String declaredThing() {
        return right.toString();
    }

    @Override public String contents ()
    {
        String leftEqual = left.contents() + " = ";

        String candidate = leftEqual + right.contents();
        if (candidate.length() <= contentsBudget())
            return candidate;

        candidate = leftEqual + "(?)";
        return candidate.length() <= contentsBudget()
                ? candidate
                : "(?) = (?)";
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        AssignmentNode other = (AssignmentNode) obj;
        return this.left.equals(other.left)
                && this.right.equals(other.right);
    }

    @Override
    public String toString () {
        return "Assignment:" + left + "=" + right;
    }
}