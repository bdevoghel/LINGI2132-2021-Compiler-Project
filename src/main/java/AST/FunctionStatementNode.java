package AST;

import norswap.autumn.positions.Span;

import java.util.List;

public class FunctionStatementNode extends DeclarationNode {
    public IdentifierNode identifier;
    public FunctionArgumentsNode arguments;
    public List statement;

    public FunctionStatementNode(Span span, IdentifierNode identifier, FunctionArgumentsNode arguments) {
        super(span);
        this.identifier = identifier;
        this.arguments = arguments;
    }

    public FunctionStatementNode(Span span, IdentifierNode identifier, FunctionArgumentsNode arguments, List statement) {
        super(span);
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

    /**
     * Returns a <b>brief</b> overview of the content of the node, suitable to be printed
     * in a single line.
     */
    @Override
    public String contents() {
        return this.toString();
    }

    @Override
    public String name() {
        return identifier.toString();
    }

    @Override
    public String declaredThing() {
        return null;
    }
}
