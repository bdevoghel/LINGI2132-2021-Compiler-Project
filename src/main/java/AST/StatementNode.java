package ast;

import norswap.autumn.positions.Span;

public abstract class StatementNode extends KneghelNode {
    public StatementNode (Span span) {
        super(span);
    }
}