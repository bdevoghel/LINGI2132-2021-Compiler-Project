package AST;

public class BinaryExpressionNode implements ExpressionNode {
    private BinaryOperator operator;
    private ASTNode leftChild;
    private ASTNode rightChild;

    public BinaryExpressionNode(ASTNode leftChild, BinaryOperator operator, ASTNode rightChild) {
        this.operator = operator;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        BinaryExpressionNode other = (BinaryExpressionNode) obj;
        return
                this.operator.equals(other.operator) &&
                this.leftChild.equals(other.leftChild) &&
                this.rightChild.equals(other.rightChild);
    }

    @Override
    public String toString() {
        return "BinaryExpressionNode:" + leftChild + operator + rightChild;
    }
}
