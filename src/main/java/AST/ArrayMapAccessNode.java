package AST;

public class ArrayMapAccessNode implements ExpressionNode {
    private ExpressionNode arrayMap;
    private ExpressionNode index;

    public ArrayMapAccessNode(ExpressionNode arrayMap, ExpressionNode index) {
        this.arrayMap = arrayMap;
        this.index = index;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        ArrayMapAccessNode other = (ArrayMapAccessNode) obj;
        return this.arrayMap.equals(other.arrayMap) && this.index.equals(other.index);
    }

    @Override
    public String toString() {
        return "ArrayMapAccessNode:" + arrayMap + "[" + index + "]";
    }
}
