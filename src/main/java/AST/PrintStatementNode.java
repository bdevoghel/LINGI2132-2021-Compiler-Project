package AST;

import norswap.autumn.positions.Span;

public class PrintStatementNode extends ExpressionNode {
    private ExpressionNode toPrint;

    public PrintStatementNode(Span span, ExpressionNode toPrint) {
        super(span);
        this.toPrint = toPrint;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        PrintStatementNode other = (PrintStatementNode) obj;
        return this.toPrint.equals(other.toPrint);
    }

    @Override
    public String toString() {
        return "PrintStatementNode:" + toPrint;
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
