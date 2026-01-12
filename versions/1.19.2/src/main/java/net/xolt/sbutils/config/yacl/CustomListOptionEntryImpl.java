package net.xolt.sbutils.config.yacl;

import dev.isxander.yacl.api.*;
import dev.isxander.yacl.api.utils.Dimension;
import dev.isxander.yacl.gui.AbstractWidget;
import dev.isxander.yacl.gui.YACLScreen;
import dev.isxander.yacl.gui.controllers.ListEntryWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class CustomListOptionEntryImpl<T> implements ListOptionEntry<T> {
    private final CustomListOptionImpl<T> group;
    private T value;
    private final Binding<T> binding;
    private final Controller<T> controller;

    CustomListOptionEntryImpl(CustomListOptionImpl<T> group, T initialValue, @NotNull Function<ListOptionEntry<T>, Controller<T>> controlGetter) {
        this.group = group;
        this.value = initialValue;
        this.binding = new EntryBinding();
        this.controller = new EntryController<T>((Controller)controlGetter.apply(this), this);
    }

    public @NotNull Component name() {
        return Component.empty();
    }

    public @NotNull Component tooltip() {
        return Component.empty();
    }

    public @NotNull Controller<T> controller() {
        return this.controller;
    }

    public @NotNull Binding<T> binding() {
        return this.binding;
    }

    public boolean available() {
        return this.parentGroup().available();
    }

    public void setAvailable(boolean available) {
    }

    public ListOption<T> parentGroup() {
        return this.group;
    }

    public boolean changed() {
        return false;
    }

    public @NotNull T pendingValue() {
        return this.value;
    }

    public void requestSet(T value) {
        this.binding.setValue(value);
    }

    public boolean applyValue() {
        return false;
    }

    public void forgetPendingValue() {
    }

    public void requestSetDefault() {
    }

    public boolean isPendingValueDefault() {
        return false;
    }

    public boolean canResetToDefault() {
        return false;
    }

    public void addListener(BiConsumer<Option<T>, T> changedListener) {
    }

    @Environment(EnvType.CLIENT)
    @ApiStatus.Internal
    public static record EntryController<T>(Controller<T> controller, CustomListOptionEntryImpl<T> entry) implements Controller<T> {
        public EntryController(Controller<T> controller, CustomListOptionEntryImpl<T> entry) {
            this.controller = controller;
            this.entry = entry;
        }

        public Option<T> option() {
            return this.controller.option();
        }

        public Component formatValue() {
            return this.controller.formatValue();
        }

        public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
            return new ListEntryWidget(screen, this.entry, this.controller.provideWidget(screen, widgetDimension));
        }

        public Controller<T> controller() {
            return this.controller;
        }

        public CustomListOptionEntryImpl<T> entry() {
            return this.entry;
        }
    }

    @Environment(EnvType.CLIENT)
    private class EntryBinding implements Binding<T> {
        private EntryBinding() {
        }

        public void setValue(T newValue) {
            CustomListOptionEntryImpl.this.value = newValue;
            CustomListOptionEntryImpl.this.group.callListeners();
        }

        public T getValue() {
            return CustomListOptionEntryImpl.this.value;
        }

        public T defaultValue() {
            throw new UnsupportedOperationException();
        }
    }
}
