package AST;

import norswap.autumn.positions.Span;

import java.util.List;

public class WhileStatementNode extends StatementNode {
    public ExpressionNode condition;
    public List<StatementNode> statements;

    public WhileStatementNode(Span span, ExpressionNode condition, List<StatementNode> statements) {
        super(span);
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

    /**
     * Returns a <b>brief</b> overview of the content of the node, suitable to be printed
     * in a single line.
     */
    @Override
    public String contents() {
        return this.toString();
    }
}
