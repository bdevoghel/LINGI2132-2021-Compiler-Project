package ast;

import norswap.autumn.positions.Span;

public final class IntLiteralNode extends ExpressionNode
{
    public final long value;

    public IntLiteralNode (Span span, long value) {
        super(span);
        this.value = value;
    }

    @Override public String contents() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        IntLiteralNode other = (IntLiteralNode) obj;
        return this.value == other.value;
    }

    @Override
    public String toString () {
        return "IntLiteral:" + value;
    }
}