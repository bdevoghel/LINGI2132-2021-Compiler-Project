package AST;

public class IdentifierNode implements ExpressionNode {
    private String value;

    public IdentifierNode(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        IdentifierNode other = (IdentifierNode) obj;
        return this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "IdentifierNode:" + value;
    }
}