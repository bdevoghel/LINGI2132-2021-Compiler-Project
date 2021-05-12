package ast;

public enum UnaryOperator
{
    NOT("!"),
    NEG("-");

    public final String string;

    UnaryOperator (String string) {
        this.string = string;
    }
}
