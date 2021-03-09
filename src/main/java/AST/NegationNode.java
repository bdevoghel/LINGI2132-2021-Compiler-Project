package AST;

public class NegationNode implements ASTNode {
    private String value;

    public NegationNode(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        NegationNode other = (NegationNode) obj;
        return this.value.equals(other.value);
    }
}
