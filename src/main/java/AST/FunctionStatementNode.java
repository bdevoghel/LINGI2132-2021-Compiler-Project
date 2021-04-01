package AST;

import norswap.autumn.positions.Span;

import java.util.List;

public class FunctionStatementNode extends DeclarationNode {
    public IdentifierNode identifier;
    public FunctionArgumentsNode arguments;
    public List<StatementNode> statements;

    public FunctionStatementNode(Span span, IdentifierNode identifier, FunctionArgumentsNode arguments) {
        this(span, identifier, arguments, null);
    }

    public FunctionStatementNode(Span span, IdentifierNode identifier, FunctionArgumentsNode arguments, List<StatementNode> statements) {
        super(span);
        this.identifier = identifier;
        this.arguments = arguments;
        this.statements = statements;
    }

    public FunctionStatementNode setStatements(List<StatementNode> statements) {
        this.statements = statements;
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
                (this.statements == null && other.statements == null || this.statements.equals(other.statements));
    }

    @Override
    public String toString() {
        return "FunctionStatementNode:[" + identifier + "(" + arguments + "){" + statements + "}"+"]";
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
        return identifier.getValue();
    }

    @Override
    public String declaredThing() {
        return null;
    }
}
