package AST;

import java.util.List;

public class ClassStatementNode implements StatementNode {
    private  IdentifierNode identifier;
    private List functions;

    public ClassStatementNode(List functions) {
        this.functions = functions;
    }

    public ClassStatementNode(IdentifierNode identifier, List functions) {
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
}
