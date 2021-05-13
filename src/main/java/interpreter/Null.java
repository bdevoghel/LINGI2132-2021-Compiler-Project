package interpreter;

/**
 * Singleton class representing the null value in the Kneghel interpreter.
 */
public final class Null {
    public static final Null INSTANCE = new Null();
    private Null() {}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Null){
            return true;
        }
        return false;
    }
}
