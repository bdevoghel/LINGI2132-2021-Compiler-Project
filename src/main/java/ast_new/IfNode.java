package ast_new;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class IfNode extends StatementNode
{
    public final ExpressionNode condition;
    public final StatementNode trueStatement;
    public final StatementNode falseStatement;

    public IfNode (Span span, Object condition, Object trueStatement, Object falseStatement) {
        super(span);
        this.condition = Util.cast(condition, ExpressionNode.class);
        this.trueStatement = Util.cast(trueStatement, StatementNode.class);
        this.falseStatement = falseStatement == null
                ? null
                : Util.cast(falseStatement, StatementNode.class);
    }

    @Override public String contents ()
    {
        String condition = this.condition.contents();
        String candidate = falseStatement == null
                ? String.format("if %s ...", condition)
                : String.format("if %s ... else ...", condition);

        return candidate.length() <= contentsBudget()
                ? candidate
                : falseStatement == null
                ? "if (?) ..."
                : "if (?) ... else ...";
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        IfNode other = (IfNode) obj;
        return this.condition.equals(other.condition)
                && this.trueStatement.equals(other.trueStatement)
                && (this.falseStatement == null && other.falseStatement == null || this.falseStatement.equals(other.falseStatement));
    }

    @Override
    public String toString () {
        return "If(" + condition + "){" + trueStatement + "}{" + falseStatement + "}";
    }
}
