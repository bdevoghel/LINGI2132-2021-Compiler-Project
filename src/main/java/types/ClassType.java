package types;

public final class ClassType extends Type
{
    public static final ClassType INSTANCE = new ClassType();
    private ClassType() {}

    @Override public String name() {
        return "Class";
    }
}
