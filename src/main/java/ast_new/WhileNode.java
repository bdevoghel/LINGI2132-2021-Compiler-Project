package ast_new;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class WhileNode extends StatementNode
{
    public final ExpressionNode condition;
    public final StatementNode body;

    public WhileNode (Span span, Object condition, Object body) {
        super(span);
        this.condition = Util.cast(condition, ExpressionNode.class);
        this.body = Util.cast(body, StatementNode.class);
    }

    @Override public String contents ()
    {
        String candidate = String.format("while %s ...", condition.contents());

        return candidate.length() <= contentsBudget()
                ? candidate
                : "while (?) ...";
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        WhileNode other = (WhileNode) obj;
        return this.condition.equals(other.condition)
                && this.body.equals(other.body);
    }

    @Override
    public String toString () {
        return "While(" + condition + "){" + body + "}";
    }
}
