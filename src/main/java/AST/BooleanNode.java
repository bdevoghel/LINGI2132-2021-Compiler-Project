package AST;

public class BooleanNode implements ASTNode{
    private boolean value;

    public BooleanNode(boolean value){
        this.value = value;
    }

    @Override
    public boolean equals(Object o){
        if (getClass() != o.getClass()) {
            return false;
        }

        BooleanNode other = (BooleanNode) o;
        return this.value == other.value;
    }
}
