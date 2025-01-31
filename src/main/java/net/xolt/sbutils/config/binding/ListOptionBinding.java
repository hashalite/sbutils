package net.xolt.sbutils.config.binding;

import net.xolt.sbutils.config.binding.constraints.ListConstraints;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ListOptionBinding<C, T> extends ConfigBinding<C, List<T>> {
    private final Class<T> listType;
    private final T initialValue;

    public ListOptionBinding(String namespace, String path, @NotNull T initialValue, Class<T> listType, Function<C, List<T>> get, BiConsumer<C, List<T>> set) {
        this(namespace, path, initialValue, listType, get, set, null);
    }

    public ListOptionBinding(String namespace, String path, @NotNull T initialValue, Class<T> listType, Function<C, List<T>> get, BiConsumer<C, List<T>> set, @Nullable ListConstraints<T> constraints) {
        super(namespace, path, get, set, constraints);
        this.listType = listType;
        this.initialValue = initialValue;
    }

    public Class<T> getListType() {
        return listType;
    }

    public T getInitialValue() {
        return initialValue;
    }
}
