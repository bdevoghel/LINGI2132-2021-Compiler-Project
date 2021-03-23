package AST;

import norswap.autumn.positions.Span;

public abstract class ASTNode {

    public final Span span;

    protected ASTNode (Span span) {
        this.span = span;
    }

    @Override
    public boolean equals(Object obj){
        if (getClass()  != obj.getClass())
            return false;
        ASTNode other = (ASTNode) obj;
        return this.span.equals(other.span);
    }

    /**
     * Returns a <b>brief</b> overview of the content of the node, suitable to be printed
     * in a single line.
     */
    public abstract String contents();


    /**
     * The size that the string returned by {@link #toString} should not exceed.
     */
    public static int TO_STRING_CUTOFF = 40;

    int contentsBudget () {
        return TO_STRING_CUTOFF - getClass().getSimpleName().length() - 2;
        // 2 == "()".length() - "Node".length
    }

}
