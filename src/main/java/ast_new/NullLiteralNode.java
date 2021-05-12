package ast_new;

import norswap.autumn.positions.Span;

public final class NullLiteralNode extends ExpressionNode
{
    public NullLiteralNode(Span span) {
        super(span);
    }

    @Override public String contents() {
        return "null";
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        return true;
    }

    @Override
    public String toString () {
        return "NullLiteral";
    }
}
