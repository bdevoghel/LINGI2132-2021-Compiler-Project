package scopes;

import AST.DeclarationNode;

public final class DeclarationContext
{
    public final Scope scope;
    public final DeclarationNode declaration;

    public DeclarationContext(Scope scope, DeclarationNode declaration) {
        this.scope = scope;
        this.declaration = declaration;
    }
}
