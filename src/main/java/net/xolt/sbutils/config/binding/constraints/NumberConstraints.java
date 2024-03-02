package net.xolt.sbutils.config.binding.constraints;

import net.xolt.sbutils.config.binding.Constraints;
import org.jetbrains.annotations.Nullable;

public class NumberConstraints<T extends Number & Comparable<T>> implements Constraints<T> {
    private final T min;
    private final T max;

    public NumberConstraints(@Nullable T min, @Nullable T max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public T validate(T input) {
        if (min != null && input.compareTo(min) < 0)
            return min;
        if (max != null && input.compareTo(max) > 0)
            return max;
        return input;
    }

    @Override
    public boolean isValid(T input) {
        if (min != null && input.compareTo(min) < 0)
            return true;
        if (max != null && input.compareTo(max) > 0)
            return true;
        return false;
    }

    public T getMin() {
        return min;
    }

    public T getMax() {
        return max;
    }
}
