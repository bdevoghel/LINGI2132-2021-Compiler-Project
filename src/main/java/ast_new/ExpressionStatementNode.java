package ast_new;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public class ExpressionStatementNode extends StatementNode
{
    public final ExpressionNode expression;

    public ExpressionStatementNode (Span span, Object expression) {
        super(span);
        this.expression = Util.cast(expression, ExpressionNode.class);
    }

    @Override public String contents () {
        return expression.contents();
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        ExpressionStatementNode other = (ExpressionStatementNode) obj;
        return this.expression.equals(other.expression);
    }

    @Override
    public String toString () {
        return "ExpressionStatement:" + expression;
    }
}