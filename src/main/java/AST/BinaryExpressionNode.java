package AST;

import norswap.autumn.positions.Span;

public class BinaryExpressionNode extends ExpressionNode {
    public BinaryOperator operator;
    public ASTNode leftChild;
    public ASTNode rightChild;

    public BinaryExpressionNode(Span span, ASTNode leftChild, BinaryOperator operator, ASTNode rightChild) {
        super(span);
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

    @Override public String contents ()
    {
        String candidate = String.format("%s %s %s",
                leftChild.contents(), operator, rightChild.contents());

        return candidate.length() <= contentsBudget()
                ? candidate
                : String.format("(?) %s (?)", operator);
    }

    @Override
    public String toString() {
        return "BinaryExpressionNode:" + leftChild + operator + rightChild;
    }
}
