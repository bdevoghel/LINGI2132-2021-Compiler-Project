package AST;

public class PrintStatementNode implements ExpressionNode {
    private ExpressionNode toPrint;

    public PrintStatementNode(ExpressionNode toPrint) {
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
}
