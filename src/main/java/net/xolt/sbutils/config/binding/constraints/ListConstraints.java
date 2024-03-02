package net.xolt.sbutils.config.binding.constraints;

import net.xolt.sbutils.config.binding.Constraints;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ListConstraints<T> implements Constraints<List<T>> {
    private final Boolean allowDupes;
    private final Integer maxSize;
    private final Constraints<T> entryConstraints;

    public ListConstraints(@Nullable Boolean allowDupes, @Nullable Integer maxSize, @Nullable Constraints<T> entryConstraints) {
        this.allowDupes = allowDupes == null ? false : allowDupes;
        this.maxSize = maxSize == null ? Integer.MAX_VALUE : maxSize;
        this.entryConstraints = entryConstraints;
    }

    @Override
    public List<T> validate(List<T> input) {
        if (isValid(input))
            return input;

        List<T> result = new ArrayList<>(input);
        if (!allowDupes) {
            List<T> withoutDupes = withoutDupes(input);
            if (input.size() != withoutDupes.size())
                result = withoutDupes;
        }

        while (result.size() > maxSize)
            result.remove(input.size() - 1);

        if (entryConstraints != null)
            result = result.stream().map(entryConstraints::validate).toList();

        return result;
    }

    @Override
    public boolean isValid(List<T> input) {
        return !((!allowDupes && hasDupes(input)) ||  (input.size() > maxSize) || (entryConstraints != null && input.stream().anyMatch((item) -> !entryConstraints.isValid(item))));
    }

    public boolean getAllowDupes() {
        return allowDupes;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public Constraints<T> getEntryConstraints() {
        return entryConstraints;
    }

    private static <S> List<S> withoutDupes(List<S> input) {
        return new ArrayList<>(new LinkedHashSet<>(input));
    }

    private static boolean hasDupes(List<?> input) {
        return input.size() != withoutDupes(input).size();
    }
}
