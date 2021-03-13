package AST;

public class ReturnStatementNode implements StatementNode {
    private  ExpressionNode value;

    public ReturnStatementNode(ExpressionNode value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        ReturnStatementNode other = (ReturnStatementNode) obj;
        return
                this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "ReturnStatementNode:" + value;
    }
}
