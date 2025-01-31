package net.xolt.sbutils.config.binding;

import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class OptionBinding<C, T> extends ConfigBinding<C, T> {
    private final Class<T> type;

    public OptionBinding(String namespace, String path, Class<T> type, Function<C, T> get, BiConsumer<C, T> set) {
        this(namespace, path, type, get, set, null);
    }

    public OptionBinding(String namespace, String path, Class<T> type, Function<C, T> get, BiConsumer<C, T> set, @Nullable Constraints<T> constraints) {
        super(namespace, path, get, set, constraints);
        this.type = type;
    }

    public Class<T> getType() {
        return type;
    }
}
