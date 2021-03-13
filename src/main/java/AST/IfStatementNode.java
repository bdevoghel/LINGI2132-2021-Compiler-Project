package AST;

import java.util.List;

public class IfStatementNode implements StatementNode {
    private ExpressionNode condition;
    private List trueStatements;
    private List falseStatements;

    public IfStatementNode(ExpressionNode condition, List trueStatements) {
        this(condition, trueStatements, null);
    }

    public IfStatementNode(ExpressionNode condition, List trueStatements, List falseStatements) {
        this.condition = condition;
        this.trueStatements = trueStatements;
        this.falseStatements = falseStatements;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        IfStatementNode other = (IfStatementNode) obj;
        return
                this.condition.equals(other.condition) &&
                this.trueStatements.equals(other.trueStatements) &&
                (this.falseStatements == null && other.falseStatements == null || this.falseStatements.equals(other.falseStatements));
    }

    @Override
    public String toString() {
        return "IfStatementNode:[" + condition + "{" + trueStatements + "}" + (falseStatements == null ? "" : falseStatements) +"]";
    }
}
