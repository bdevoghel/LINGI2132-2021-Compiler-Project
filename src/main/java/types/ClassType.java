package types;

import norswap.utils.NArrays;
import java.util.Arrays;

public final class ClassType extends Type
{
    public final Type[] funTypes;

    public ClassType (Type... funTypes) {
        this.funTypes = funTypes;
    }

    @Override public String name() {
        String[] funs = NArrays.map(funTypes, new String[0], Type::name);
        return String.format("(%s) -> %s", String.join(",", funs));
    }

    @Override public boolean equals (Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassType)) return false;
        ClassType other = (ClassType) o;

        return Arrays.equals(funTypes, other.funTypes);
    }

    @Override public int hashCode () {
        return Arrays.hashCode(funTypes);
    }
}