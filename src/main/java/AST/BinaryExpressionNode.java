package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class BinaryExpressionNode extends ExpressionNode
{
    public final ExpressionNode left, right;
    public final BinaryOperator operator;

    public BinaryExpressionNode (Span span, Object left, Object operator, Object right) {
        super(span);
        this.left = Util.cast(left, ExpressionNode.class);
        this.right = Util.cast(right, ExpressionNode.class);
        this.operator = Util.cast(operator, BinaryOperator.class);
    }

    @Override public String contents ()
    {
        String candidate = String.format("%s %s %s",
                left.contents(), operator.string, right.contents());

        return candidate.length() <= contentsBudget()
                ? candidate
                : String.format("(?) %s (?)", operator.string);
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        BinaryExpressionNode other = (BinaryExpressionNode) obj;
        return this.operator.equals(other.operator)
                && this.left.equals(other.left)
                && this.right.equals(other.right);
    }

    @Override
    public String toString () {
        return "BinaryExpression:" + left + operator + right;
    }
}
