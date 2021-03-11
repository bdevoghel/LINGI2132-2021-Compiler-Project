package AST;

import java.util.List;

public class FunctionStatementNode implements StatementNode {
    private IdentifierNode identifier;
    private FunctionArgumentsNode arguments;
    private List statement;
    private ExpressionNode returnExpression;

    public FunctionStatementNode(IdentifierNode identifier, FunctionArgumentsNode arguments) {
        this.identifier = identifier;
        this.arguments = arguments;
    }

    public FunctionStatementNode(IdentifierNode identifier, FunctionArgumentsNode arguments, List statement, ExpressionNode returnExpression) {
        this.identifier = identifier;
        this.arguments = arguments;
        this.statement = statement;
        this.returnExpression = returnExpression;
    }

    public FunctionStatementNode setStatement(List statement) {
        this.statement = statement;
        return this;
    }

    public FunctionStatementNode setReturnExpression(ExpressionNode returnExpression) {
        this.returnExpression = returnExpression;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        FunctionStatementNode other = (FunctionStatementNode) obj;
        return
                this.identifier.equals(other.identifier) &&
                this.arguments.equals(other.arguments) &&
                (this.statement == null && other.statement == null || this.statement.equals(other.statement)) &&
                (this.returnExpression == null && other.returnExpression == null || this.returnExpression.equals(other.returnExpression));
    }

    @Override
    public String toString() {
        return "FunctionStatementNode:[" + identifier + "(" + arguments + "){" + statement + "_return:" + returnExpression + "}"+"]";
    }
}
