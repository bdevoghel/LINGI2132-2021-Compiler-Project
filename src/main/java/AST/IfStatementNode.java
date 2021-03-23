package AST;

import norswap.autumn.positions.Span;

import java.util.List;

public class IfStatementNode extends StatementNode {
    private ExpressionNode condition;
    private List trueStatements;
    private List falseStatements;

    public IfStatementNode(Span span, ExpressionNode condition, List trueStatements) {
        this(span, condition, trueStatements, null);
    }

    public IfStatementNode(Span span, ExpressionNode condition, List trueStatements, List falseStatements) {
        super(span);
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

    /**
     * Returns a <b>brief</b> overview of the content of the node, suitable to be printed
     * in a single line.
     */
    @Override
    public String contents() {
        return this.toString();
    }
}
