package net.xolt.sbutils.config.binding;

public interface Constraints<T> {

    boolean isValid(T input);

    T validate(T input);
}
