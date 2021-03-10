package AST;

public class StringNode implements ExpressionNode {
    private String value;

    public StringNode(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        StringNode other = (StringNode) obj;
        return this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "StringNode:" + value;
    }
}
