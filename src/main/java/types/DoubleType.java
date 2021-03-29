package types;

public final class DoubleType extends Type
{
    public static final DoubleType INSTANCE = new DoubleType();
    private DoubleType () {}

    @Override public boolean isPrimitive () {
        return true;
    }

    @Override public String name() {
        return "Double";
    }
}