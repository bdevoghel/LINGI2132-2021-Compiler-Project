package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class ReferenceNode extends ExpressionNode
{
    public final String name;

    public ReferenceNode (Span span, Object name) {
        super(span);
        this.name = Util.cast(name, String.class);
    }

    @Override public String contents() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        ReferenceNode other = (ReferenceNode) obj;
        return this.name.equals(other.name);
    }

    @Override
    public String toString () {
        return "Reference:" + name;
    }
}
