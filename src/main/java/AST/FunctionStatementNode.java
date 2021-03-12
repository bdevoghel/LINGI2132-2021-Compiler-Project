package AST;

import java.util.List;

public class FunctionStatementNode implements StatementNode {
    private IdentifierNode identifier;
    private FunctionArgumentsNode arguments;
    private List statement;

    public FunctionStatementNode(IdentifierNode identifier, FunctionArgumentsNode arguments) {
        this.identifier = identifier;
        this.arguments = arguments;
    }

    public FunctionStatementNode(IdentifierNode identifier, FunctionArgumentsNode arguments, List statement) {
        this.identifier = identifier;
        this.arguments = arguments;
        this.statement = statement;
    }

    public FunctionStatementNode setStatement(List statement) {
        this.statement = statement;
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
                (this.statement == null && other.statement == null || this.statement.equals(other.statement));
    }

    @Override
    public String toString() {
        return "FunctionStatementNode:[" + identifier + "(" + arguments + "){" + statement + "}"+"]";
    }
}
