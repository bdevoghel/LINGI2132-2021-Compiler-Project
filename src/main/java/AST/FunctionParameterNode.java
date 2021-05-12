package AST;

import norswap.autumn.positions.Span;

import java.util.Objects;

public class FunctionParameterNode extends DeclarationNode {
    public IdentifierNode param;

    public FunctionParameterNode(Span span, IdentifierNode param) {
        super(span);
        this.param = param;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionParameterNode that = (FunctionParameterNode) o;
        return Objects.equals(param, that.param);
    }

    @Override
    public String toString() {
        return "FunctionParameterNode:" + param.value;
    }


    @Override
    public String contents() {
        return param.getValue();
    }

    @Override
    public String name() {
        return param.toString();
    }

    @Override
    public String declaredThing() {
        return null;
    }
}