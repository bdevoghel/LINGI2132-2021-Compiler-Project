package AST;

public class UnaryExpressionNode implements ExpressionNode {
    private UnaryOperator operator;
    private ExpressionNode operand;

    public UnaryExpressionNode(UnaryOperator operator, ExpressionNode operand) {
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
}
