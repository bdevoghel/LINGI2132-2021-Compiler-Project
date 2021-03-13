package AST;

import java.util.List;

public class WhileStatementNode implements StatementNode {
    private ExpressionNode condition;
    private List statements;

    public WhileStatementNode(ExpressionNode condition, List statements) {
        this.condition = condition;
        this.statements = statements;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        WhileStatementNode other = (WhileStatementNode) obj;
        return
                this.condition.equals(other.condition) &&
                this.statements.equals(other.statements);
    }

    @Override
    public String toString() {
        return "WhileStatementNode:[" + condition + "{" + statements + "}"+"]";
    }
}
