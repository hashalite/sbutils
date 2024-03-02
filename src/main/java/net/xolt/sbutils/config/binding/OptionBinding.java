package net.xolt.sbutils.config.binding;

import net.xolt.sbutils.config.ModConfig;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class OptionBinding<T> extends ConfigBinding<T> {
    private final Class<T> type;

    public OptionBinding(String path, Class<T> type, Function<ModConfig, T> get, BiConsumer<ModConfig, T> set) {
        this(path, type, get, set, null);
    }

    public OptionBinding(String path, Class<T> type, Function<ModConfig, T> get, BiConsumer<ModConfig, T> set, @Nullable Constraints<T> constraints) {
        super(path, get, set, constraints);
        this.type = type;
    }

    public Class<T> getType() {
        return type;
    }
}
