package ast_new;

import norswap.autumn.positions.Span;
import norswap.utils.Util;
import java.util.List;

public class FunDeclarationNode extends DeclarationNode
{
    public final String name;
    public final List<ParameterNode> parameters;
//    public final TypeNode returnType;
    public final BlockNode block;

    @SuppressWarnings("unchecked")
    public FunDeclarationNode
            (Span span, Object name, Object parameters, /*Object returnType, */Object block) {
        super(span);
        this.name = Util.cast(name, String.class);
        this.parameters = Util.cast(parameters, List.class);
//        this.returnType = returnType == null
//                ? new SimpleTypeNode(new Span(span.start, span.start), "Void")
//                : Util.cast(returnType, TypeNode.class);
        this.block = Util.cast(block, BlockNode.class);
    }

    @Override public String name () {
        return name;
    }

    @Override public String contents () {
        return "fun " + name;
    }

    @Override public String declaredThing () {
        return "function";
    }

    @Override
    public boolean equals(Object obj) {
        if (getClass()  != obj.getClass())
            return false;
        FunDeclarationNode other = (FunDeclarationNode) obj;
        return this.name.equals(other.name)
                && this.parameters.equals(other.parameters)
                && this.block.equals(other.block);
    }

    @Override
    public String toString () {
        return "Function:" + name + parameters + "::" + block;
    }
}
