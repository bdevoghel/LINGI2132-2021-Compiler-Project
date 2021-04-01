package AST;

import norswap.autumn.positions.Span;

public abstract class ExpressionNode extends ASTNode {

    public ExpressionNode (Span span) {
        super(span);
    }

    @Override
    public boolean equals(Object obj){
        if (getClass()  != obj.getClass())
            return false;
        ExpressionNode other = (ExpressionNode) obj;
        return this.span.equals(other.span);
    }

}
