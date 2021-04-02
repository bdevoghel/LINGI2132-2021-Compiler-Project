package types;

import norswap.utils.NArrays;
import java.util.Arrays;

public final class FunType extends Type
{
    public final Type[] paramTypes;

    public FunType (Type... paramTypes) {
        this.paramTypes = paramTypes;
    }

    @Override public String name() {
        String[] params = NArrays.map(paramTypes, new String[0], Type::name);
        return params.length > 0
                ? String.format("%s{%s}", "FunType", String.join(",", params))
                : "EmptyFunType";
    }

    @Override public boolean equals (Object o) {
        if (this == o) return true;
        if (!(o instanceof FunType)) return false;
        FunType other = (FunType) o;

        return Arrays.equals(paramTypes, other.paramTypes);
    }

    @Override public int hashCode () {
        return Arrays.hashCode(paramTypes);
    }

    @Override
    public String toString() {
        return "FunType{" +
                "paramTypes=" + Arrays.toString(paramTypes) +
                '}';
    }
}