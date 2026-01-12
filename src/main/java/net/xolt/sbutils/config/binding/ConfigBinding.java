package net.xolt.sbutils.config.binding;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class ConfigBinding<C, T> {

    private final String namespace;
    private final String path;
    private final Function<C, T> get;
    private final BiConsumer<C, T> set;
    private final Constraints<T> constraints;
    private final List<BiConsumer<T, T>> listeners;

    public ConfigBinding(String namespace, String path, Function<C, T> get, BiConsumer<C, T> set) {
        this(namespace, path, get, set, null);
    }

    public ConfigBinding(String namespace, String path, Function<C, T> get, BiConsumer<C, T> set, @Nullable Constraints<T> constraints) {
        this.namespace = namespace;
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
        return getTranslation() + ".tooltip";
    }

    public String getTranslation() {
        return "text." + namespace + ".config.option." + path;
    }

    public Constraints<T> getConstraints() {
        return constraints;
    }

    public T get(C instance) {
        return get.apply(instance);
    }

    public T get(ConfigClassHandler<C> instance) {
        //? yacl: >=3.2.0 {
        return get(instance.instance());
        //? } else
        //return get(instance.getConfig());
    }

    public void set(C instance, T newValue) {
        T oldValue = get.apply(instance);
        T validated = constraints == null ? newValue : constraints.validate(newValue);
        set.accept(instance, validated);
        for (BiConsumer<T, T> listener : listeners)
            listener.accept(oldValue, validated);
    }

    public void set(ConfigClassHandler<C> instance, T newValue) {
        //? yacl: >=3.2.0 {
        set(instance.instance(), newValue);
         //? } else
        //set(instance.getConfig(), newValue);
    }

    public void addListener(BiConsumer<T, T> listener) {
        listeners.add(listener);
    }
}
