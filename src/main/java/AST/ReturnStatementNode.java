package AST;

import norswap.autumn.positions.Span;

public class ReturnStatementNode extends StatementNode {
    private  ExpressionNode value;

    public ReturnStatementNode(Span span, ExpressionNode value) {
        super(span);
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        ReturnStatementNode other = (ReturnStatementNode) obj;
        return
                this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return "ReturnStatementNode:" + value;
    }

    /**
     * Returns a <b>brief</b> overview of the content of the node, suitable to be printed
     * in a single line.
     */
    @Override
    public String contents() {
        return this.toString();
    }
}
