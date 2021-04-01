package AST;

import norswap.autumn.positions.Span;

import java.util.List;

public class ClassStatementNode extends DeclarationNode {
    public IdentifierNode identifier;
    public List<FunctionStatementNode> functions;

    public ClassStatementNode(Span span, List functions) {
        this(span, null, functions);
    }

    public ClassStatementNode(Span span, IdentifierNode identifier, List functions) {
        super(span);
        this.identifier = identifier;
        this.functions = functions;
    }

    public ClassStatementNode setIdentifier(IdentifierNode identifier) {
        this.identifier = identifier;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        ClassStatementNode other = (ClassStatementNode) obj;
        return
                this.functions.equals(other.functions);
    }

    @Override
    public String toString() {
        return "ClassStatementNode:[" + functions + "]";
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
