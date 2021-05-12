package ast;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.List;

public final class ClassNode extends KneghelNode
{
    public final String name;
    public final List<FunDeclarationNode> functions;

    @SuppressWarnings("unchecked")
    public ClassNode (Span span, Object name, Object functions) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.functions = Util.cast(functions, List.class);
    }

//    @Override // is not a DeclarationNode
    public String name () {
        return name;
    }

    @Override
    public String contents () {
        return "class " + name;
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        ClassNode other = (ClassNode) obj;
        return this.name.equals(other.name)
                && this.functions.equals(other.functions);
    }

    @Override
    public String toString () {
        return "Class<" + name + ">:" + functions;
    }
}