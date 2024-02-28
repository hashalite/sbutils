package net.xolt.sbutils.config;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConfigBinding<T> {

    private static final String OPTION_KEY = "text.sbutils.config.option.";

    private static final String CATEGORY_KEY = "text.sbutils.config.category.";
    private final String path;
    private final Supplier<T> get;
    private final Consumer<T> set;

    public ConfigBinding(String path, Supplier<T> get, Consumer<T> set) {
        this.path = path;
        this.get = get;
        this.set = set;
    }

    public String path() {
        return path;
    }

    public MutableComponent name() {
        return Component.translatable(OPTION_KEY + path);
    }

    public MutableComponent category() {
        return Component.translatable(CATEGORY_KEY + path.split("\\.")[0]);
    }

    public T get() {
        return get.get();
    }

    public void set(T newValue) {
        set.accept(newValue);
    }
}
