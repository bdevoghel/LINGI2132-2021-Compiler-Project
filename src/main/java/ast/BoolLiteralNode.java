package ast;

import norswap.autumn.positions.Span;

public final class BoolLiteralNode extends ExpressionNode
{
    public final boolean value;

    public BoolLiteralNode(Span span, boolean value) {
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
        BoolLiteralNode other = (BoolLiteralNode) obj;
        return this.value == other.value;
    }

    @Override
    public String toString () {
        return "BoolLiteral:" + value;
    }
}
