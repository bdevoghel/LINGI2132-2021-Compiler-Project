package ast_new;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

public final class ArrayAccessNode extends ExpressionNode
{
    public final ExpressionNode array;
    public final ExpressionNode index;

    public ArrayAccessNode (Span span, Object array, Object index) {
        super(span);
        this.array = Util.cast(array, ExpressionNode.class);
        this.index = Util.cast(index, ExpressionNode.class);
    }

    @Override public String contents() {
        return String.format("%s[%s]", array.contents(), index.contents());
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        ArrayAccessNode other = (ArrayAccessNode) obj;
        return this.array.equals(other.array)
                && this.index.equals(other.index);
    }

    @Override
    public String toString () {
        return "ArrayAccess:" + array + "[" + index + "]";
    }
}
