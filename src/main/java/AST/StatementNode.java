package AST;

import norswap.autumn.positions.Span;

public abstract class StatementNode extends ASTNode {

    public StatementNode (Span span) {
        super(span);
    }

    @Override
    public boolean equals(Object obj){
        if (getClass()  != obj.getClass())
            return false;
        StatementNode other = (StatementNode) obj;
        return this.span.equals(other.span);
    }

}
