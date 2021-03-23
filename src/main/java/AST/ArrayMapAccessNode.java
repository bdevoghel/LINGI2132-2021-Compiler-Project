package AST;

import norswap.autumn.positions.Span;

public class ArrayMapAccessNode extends ExpressionNode {
    private IdentifierNode arrayMap;
    private ExpressionNode index;

    public ArrayMapAccessNode(Span span, IdentifierNode arrayMap, ExpressionNode index) {
        super(span);
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

    @Override public String contents () {
        return this.toString();
    }

    @Override
    public String toString() {
        return "ArrayMapAccessNode:" + arrayMap + "[" + index + "]";
    }
}
