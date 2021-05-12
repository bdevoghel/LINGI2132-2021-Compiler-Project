package ast;

import norswap.autumn.positions.Span;

public final class FloatLiteralNode extends ExpressionNode
{
    public final double value;

    public FloatLiteralNode (Span span, double value) {
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
        FloatLiteralNode other = (FloatLiteralNode) obj;
        return this.value == other.value;
    }

    @Override
    public String toString () {
        return "FloatLiteral:" + value;
    }
}
