package AST;

import norswap.autumn.positions.Span;

public class UnaryExpressionNode extends ExpressionNode {
    private UnaryOperator operator;
    private ExpressionNode operand;

    public UnaryExpressionNode(Span span, UnaryOperator operator, ExpressionNode operand) {
        super(span);
        this.operator = operator;
        this.operand = operand;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        UnaryExpressionNode other = (UnaryExpressionNode) obj;
        return
                this.operator.equals(other.operator) &&
                this.operand.equals(other.operand);
    }

    @Override
    public String toString() {
        return "UnaryExpressionNode:" + operator + operand;
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
