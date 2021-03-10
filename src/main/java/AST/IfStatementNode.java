package AST;

public class IfStatementNode implements StatementNode {
    private ExpressionNode condition;
    private StatementNode trueStatement;
    private StatementNode falseStatement;

    public IfStatementNode(ExpressionNode condition, StatementNode trueStatement) {
        this(condition, trueStatement, null);
    }

    public IfStatementNode(ExpressionNode condition, StatementNode trueStatement, StatementNode falseStatement) {
        this.condition = condition;
        this.trueStatement = trueStatement;
        this.falseStatement = falseStatement;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        IfStatementNode other = (IfStatementNode) obj;
        return
                this.condition.equals(other.condition) &&
                this.trueStatement.equals(other.trueStatement) &&
                (this.falseStatement == null && other.falseStatement == null || this.falseStatement.equals(other.falseStatement));
    }

    @Override
    public String toString() {
        return "IfStatementNode:[" + condition + "{" + trueStatement + "}" + (falseStatement == null ? "" : falseStatement) +"]";
    }
}
