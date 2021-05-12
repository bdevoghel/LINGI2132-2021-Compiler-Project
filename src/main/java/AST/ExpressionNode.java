package ast;

import norswap.autumn.positions.Span;

public abstract class ExpressionNode extends KneghelNode
{
    public ExpressionNode (Span span) {
        super(span);
    }
}