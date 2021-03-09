package AST;

public class IntegerNode implements ASTNode {
    private int value;

    public IntegerNode(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        IntegerNode other = (IntegerNode) obj;
        return this.value == other.value;
    }

    @Override
    public String toString() {
        return "IntegerNode:" + value;
    }
}
