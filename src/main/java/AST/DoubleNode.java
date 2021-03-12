package AST;

public class DoubleNode implements ExpressionNode {
    private double value;

    public DoubleNode(double value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        DoubleNode other = (DoubleNode) obj;
        return this.value == other.value;
    }

    @Override
    public String toString() {
        return "DoubleNode:" + value;
    }
}
