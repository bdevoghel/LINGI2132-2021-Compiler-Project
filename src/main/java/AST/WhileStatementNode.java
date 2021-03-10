package AST;

public class WhileStatementNode implements StatementNode {
    private ExpressionNode condition;
    private StatementNode statement;

    public WhileStatementNode(ExpressionNode condition, StatementNode statement) {
        this.condition = condition;
        this.statement = statement;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        WhileStatementNode other = (WhileStatementNode) obj;
        return
                this.condition.equals(other.condition) &&
                this.statement.equals(other.statement);
    }

    @Override
    public String toString() {
        return "IfStatementNode:[" + condition + "{" + statement + "}"+"]";
    }
}
