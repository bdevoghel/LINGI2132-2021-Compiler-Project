package AST;

import norswap.autumn.positions.Span;
import norswap.utils.Util;

import java.util.List;

public final class RootNode extends ASTNode
{
    public final List<StatementNode> statements;

    @SuppressWarnings("unchecked")
    public RootNode (Span span, Object statements) {
        super(span);
        this.statements = Util.cast(statements, List.class);
    }

    @Override public String contents () {
        return "";
    }
}