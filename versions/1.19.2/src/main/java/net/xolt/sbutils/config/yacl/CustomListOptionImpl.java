package net.xolt.sbutils.config.yacl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.isxander.yacl.api.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CustomListOptionImpl<T> implements ListOption<T>{
    private final Component name;
    private final Component tooltip;
    private final Binding<List<T>> binding;
    private final T initialValue;
    private final List<ListOptionEntry<T>> entries;
    private final boolean collapsed;
    private boolean available;
    private final Class<T> typeClass;
    private final ImmutableSet<OptionFlag> flags;
    private final EntryFactory entryFactory;
    private final List<BiConsumer<Option<List<T>>, List<T>>> listeners;
    private final List<Runnable> refreshListeners;
    private final int maximumNumberOfEntries;

    public CustomListOptionImpl(@NotNull Component name, @NotNull Component tooltip, @NotNull Binding<List<T>> binding, @NotNull T initialValue, @NotNull Class<T> typeClass, @NotNull Function<ListOptionEntry<T>, Controller<T>> controllerFunction, ImmutableSet<OptionFlag> flags, boolean collapsed, boolean available, int maximumNumberOfEntries) {
        this.name = name;
        this.tooltip = tooltip;
        this.binding = binding;
        this.initialValue = initialValue;
        this.entryFactory = new EntryFactory(controllerFunction);
        this.entries = this.createEntries((Collection)this.binding().getValue());
        this.collapsed = collapsed;
        this.typeClass = typeClass;
        this.flags = flags;
        this.available = available;
        this.listeners = new ArrayList();
        this.refreshListeners = new ArrayList();
        this.maximumNumberOfEntries = maximumNumberOfEntries;
        this.callListeners();
    }

    public @NotNull Component name() {
        return this.name;
    }

    public @NotNull Component tooltip() {
        return this.tooltip;
    }

    public @NotNull ImmutableList<ListOptionEntry<T>> options() {
        return ImmutableList.copyOf(this.entries);
    }

    public @NotNull Controller<List<T>> controller() {
        throw new UnsupportedOperationException();
    }

    public @NotNull Binding<List<T>> binding() {
        return this.binding;
    }

    public @NotNull Class<List<T>> typeClass() {
        throw new UnsupportedOperationException();
    }

    public @NotNull Class<T> elementTypeClass() {
        return this.typeClass;
    }

    public boolean collapsed() {
        return this.collapsed;
    }

    public @NotNull ImmutableSet<OptionFlag> flags() {
        return this.flags;
    }

    public @NotNull ImmutableList<T> pendingValue() {
        return ImmutableList.copyOf(this.entries.stream().map(Option::pendingValue).toList());
    }

    public void insertEntry(int index, ListOptionEntry<?> entry) {
        this.entries.add(index, (ListOptionEntry<T>) entry);
        this.onRefresh();
    }

    public ListOptionEntry<T> insertNewEntryToTop() {
        ListOptionEntry<T> newEntry = this.entryFactory.create(this.initialValue);
        this.entries.add(0, newEntry);
        this.onRefresh();
        return newEntry;
    }

    public void removeEntry(ListOptionEntry<?> entry) {
        if (this.entries.remove(entry)) {
            this.onRefresh();
        }

    }

    public int indexOf(ListOptionEntry<?> entry) {
        return this.entries.indexOf(entry);
    }

    public int numberOfEntries() {
        return entries.size();
    }

    public int maximumNumberOfEntries() {
        return maximumNumberOfEntries;
    }

    public void requestSet(List<T> value) {
        this.entries.clear();
        this.entries.addAll(this.createEntries(value));
        this.onRefresh();
    }

    public boolean changed() {
        return !((List)this.binding().getValue()).equals(this.pendingValue());
    }

    public boolean applyValue() {
        if (this.changed()) {
            this.binding().setValue(this.pendingValue());
            return true;
        } else {
            return false;
        }
    }

    public void forgetPendingValue() {
        this.requestSet((List)this.binding().getValue());
    }

    public void requestSetDefault() {
        this.requestSet((List)this.binding().defaultValue());
    }

    public boolean isPendingValueDefault() {
        return ((List)this.binding().defaultValue()).equals(this.pendingValue());
    }

    public boolean available() {
        return this.available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public void addListener(BiConsumer<Option<List<T>>, List<T>> changedListener) {
        this.listeners.add(changedListener);
    }

    public void addRefreshListener(Runnable changedListener) {
        this.refreshListeners.add(changedListener);
    }

    public boolean isRoot() {
        return false;
    }

    private List<ListOptionEntry<T>> createEntries(Collection<T> values) {
        Stream<T> var10000 = values.stream();
        EntryFactory var10001 = this.entryFactory;
        Objects.requireNonNull(var10001);
        return (List)var10000.map(var10001::create).collect(Collectors.toList());
    }

    void callListeners() {
        List<T> pendingValue = this.pendingValue();
        this.listeners.forEach((listener) -> listener.accept(this, pendingValue));
    }

    private void onRefresh() {
        this.refreshListeners.forEach(Runnable::run);
        this.callListeners();
    }

    @Environment(EnvType.CLIENT)
    private class EntryFactory {
        private final Function<ListOptionEntry<T>, Controller<T>> controllerFunction;

        private EntryFactory(Function<ListOptionEntry<T>, Controller<T>> controllerFunction) {
            this.controllerFunction = controllerFunction;
        }

        public ListOptionEntry<T> create(T initialValue) {
            return new CustomListOptionEntryImpl<>(CustomListOptionImpl.this, initialValue, this.controllerFunction);
        }
    }

    @Environment(EnvType.CLIENT)
    @ApiStatus.Internal
    public static final class BuilderImpl<T> implements Builder<T> {
        private Component name = Component.empty();
        private final List<Component> tooltipLines = new ArrayList();
        private Function<ListOptionEntry<T>, Controller<T>> controllerFunction;
        private Binding<List<T>> binding = null;
        private final Set<OptionFlag> flags = new HashSet();
        private T initialValue;
        private boolean collapsed = false;
        private boolean available = true;
        private final Class<T> typeClass;
        int maximumNumberOfEntries = Integer.MAX_VALUE;

        public BuilderImpl(Class<T> typeClass) {
            this.typeClass = typeClass;
        }

        public BuilderImpl<T> name(@NotNull Component name) {
            Validate.notNull(name, "`name` must not be null", new Object[0]);
            this.name = name;
            return this;
        }

        public BuilderImpl<T> tooltip(Component... tooltips) {
            Validate.notEmpty(tooltips, "`tooltips` cannot be empty", new Object[0]);
            this.tooltipLines.addAll(List.of(tooltips));
            return this;
        }

        public BuilderImpl<T> initial(@NotNull T initialValue) {
            Validate.notNull(initialValue, "`initialValue` cannot be empty", new Object[0]);
            this.initialValue = initialValue;
            return this;
        }

        public BuilderImpl<T> controller(@NotNull Function<ListOptionEntry<T>, Controller<T>> control) {
            Validate.notNull(control, "`control` cannot be null", new Object[0]);
            this.controllerFunction = control;
            return this;
        }

        public BuilderImpl<T> binding(@NotNull Binding<List<T>> binding) {
            Validate.notNull(binding, "`binding` cannot be null", new Object[0]);
            this.binding = binding;
            return this;
        }

        public BuilderImpl<T> binding(@NotNull List<T> def, @NotNull Supplier<@NotNull List<T>> getter, @NotNull Consumer<@NotNull List<T>> setter) {
            Validate.notNull(def, "`def` must not be null", new Object[0]);
            Validate.notNull(getter, "`getter` must not be null", new Object[0]);
            Validate.notNull(setter, "`setter` must not be null", new Object[0]);
            this.binding = Binding.generic(def, getter, setter);
            return this;
        }

        public BuilderImpl<T> available(boolean available) {
            this.available = available;
            return this;
        }

        public BuilderImpl<T> flag(OptionFlag... flag) {
            Validate.notNull(flag, "`flag` must not be null", new Object[0]);
            this.flags.addAll(Arrays.asList(flag));
            return this;
        }

        public BuilderImpl<T> flags(@NotNull Collection<OptionFlag> flags) {
            Validate.notNull(flags, "`flags` must not be null", new Object[0]);
            this.flags.addAll(flags);
            return this;
        }

        public BuilderImpl<T> collapsed(boolean collapsible) {
            this.collapsed = collapsible;
            return this;
        }

        public BuilderImpl<T> maximumNumberOfEntries(int maximumNumberOfEntries) {
            this.maximumNumberOfEntries = maximumNumberOfEntries;
            return this;
        }

        public ListOption<T> build() {
            Validate.notNull(this.controllerFunction, "`controller` must not be null", new Object[0]);
            Validate.notNull(this.binding, "`binding` must not be null", new Object[0]);
            Validate.notNull(this.initialValue, "`initialValue` must not be null", new Object[0]);
            MutableComponent concatenatedTooltip = Component.empty();
            boolean first = true;

            for(Component line : this.tooltipLines) {
                if (!first) {
                    concatenatedTooltip.append("\n");
                }

                first = false;
                concatenatedTooltip.append(line);
            }

            return new CustomListOptionImpl<>(this.name, concatenatedTooltip, this.binding, this.initialValue, this.typeClass, this.controllerFunction, ImmutableSet.copyOf(this.flags), this.collapsed, this.available, maximumNumberOfEntries);
        }
    }
}
