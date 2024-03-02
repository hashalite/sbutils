package net.xolt.sbutils.config.binding;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.xolt.sbutils.config.ModConfig;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class ConfigBinding<T> {
    private static final String OPTION_KEY = "text.sbutils.config.option.";

    private final String path;
    private final Function<ModConfig, T> get;
    private final BiConsumer<ModConfig, T> set;
    private final Constraints<T> constraints;
    private final List<BiConsumer<T, T>> listeners;

    public ConfigBinding(String path, Function<ModConfig, T> get, BiConsumer<ModConfig, T> set) {
        this(path, get, set, null);
    }

    public ConfigBinding(String path, Function<ModConfig, T> get, BiConsumer<ModConfig, T> set, @Nullable Constraints<T> constraints) {
        this.path = path;
        this.get = get;
        this.set = set;
        this.constraints = constraints;
        this.listeners = new ArrayList<>();
    }

    public String getPath() {
        return path;
    }

    public MutableComponent getName() {
        return Component.translatable(getTranslation());
    }

    public MutableComponent getTooltip() {
        return Component.translatable(getTooltipTranslation());
    }

    public String getTooltipTranslation() {
        return OPTION_KEY + path + ".tooltip";
    }

    public String getTranslation() {
        return OPTION_KEY + path;
    }

    public Constraints<T> getConstraints() {
        return constraints;
    }

    public T get(ModConfig instance) {
        return get.apply(instance);
    }

    public void set(ModConfig instance, T newValue) {
        T oldValue = get.apply(instance);
        T validated = constraints == null ? newValue : constraints.validate(newValue);
        set.accept(instance, validated);
        for (BiConsumer<T, T> listener : listeners)
            listener.accept(oldValue, validated);
    }

    public void addListener(BiConsumer<T, T> listener) {
        listeners.add(listener);
    }
}
