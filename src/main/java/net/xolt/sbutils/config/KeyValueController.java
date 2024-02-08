package net.xolt.sbutils.config;

import com.google.gson.*;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class KeyValueController<K, V> implements Controller<KeyValueController.KeyValuePair<K, V>> {
    private final Option<KeyValuePair<K, V>> option;
    private final double ratio;
    private final Controller<K> keyController;
    private final Controller<V> valueController;

    public KeyValueController(Option<KeyValuePair<K, V>> option, double ratio, @Nullable String keyName, Function<Option<K>, ControllerBuilder<K>> keyController, @Nullable String valueName, Function<Option<V>, ControllerBuilder<V>> valueController) {
        this.option = option;
        this.ratio = ratio;

        this.keyController = dummyOption(keyName, keyController,
                () -> option.pendingValue().getKey(),
                (newKey) -> option.requestSet(new KeyValuePair<>(newKey, option.pendingValue().getValue()))).controller();

        this.valueController = dummyOption(valueName, valueController,
                () -> option.pendingValue().getValue(),
                (newValue) -> option.requestSet(new KeyValuePair<>(option.pendingValue().getKey(), newValue))).controller();
    }

    private static <T> Option<T> dummyOption(@Nullable String name, Function<Option<T>, ControllerBuilder<T>> controller, Supplier<T> get, Consumer<T> set) {
        return Option.<T>createBuilder()
                .name(name != null ? Text.translatable(name) : Text.literal(""))
                .binding(
                        get.get(),
                        get,
                        set
                )
                .instant(true)
                .controller(controller).build();
    }

    @Override
    public Option<KeyValuePair<K, V>> option() {
        return option;
    }

    @Override
    public Text formatValue() {
        KeyValuePair<K, V> pair = option.pendingValue();
        return keyController.formatValue().copy().append(" | ").append(valueController.formatValue());
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
        Dimension<Integer> keyDimension = widgetDimension.withWidth((int)((double)widgetDimension.width() * ratio));
        Dimension<Integer> valueDimension = widgetDimension.moved(keyDimension.width(), 0).withWidth((int)((double)widgetDimension.width() - keyDimension.width()));
        AbstractWidget keyControllerElement = keyController.provideWidget(screen, keyDimension);
        AbstractWidget valueControllerElement = valueController.provideWidget(screen, valueDimension);
        return new KeyValueControllerElement(this, screen, widgetDimension, keyControllerElement, valueControllerElement, ratio);
    }

    public static class KeyValueControllerElement extends ControllerWidget<KeyValueController<?, ?>> {
        private final AbstractWidget keyElement;
        private final AbstractWidget valueElement;
        private final double ratio;

        public KeyValueControllerElement(KeyValueController control, YACLScreen screen, Dimension<Integer> dim, AbstractWidget keyElement, AbstractWidget valueElement, double ratio) {
            super(control, screen, dim);
            this.keyElement = keyElement;
            this.valueElement = valueElement;
            this.ratio = ratio;
        }

        @Override
        public void mouseMoved(double mouseX, double mouseY) {
            keyElement.mouseMoved(mouseX, mouseY);
            valueElement.mouseMoved(mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            boolean key = keyElement.mouseClicked(mouseX, mouseY, button);
            boolean value = valueElement.mouseClicked(mouseX, mouseY, button);
            return key || value;
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            boolean key = keyElement.mouseReleased(mouseX, mouseY, button);
            boolean value = valueElement.mouseReleased(mouseX, mouseY, button);
            return  key || value;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            boolean key = keyElement.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            boolean value = valueElement.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            return key || value;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            boolean key = keyElement.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            boolean value = valueElement.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            return key || value;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            boolean key = keyElement.keyPressed(keyCode, scanCode, modifiers);
            boolean value = valueElement.keyPressed(keyCode, scanCode, modifiers);
            return key || value;
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            boolean key = keyElement.keyReleased(keyCode, scanCode, modifiers);
            boolean value = valueElement.keyReleased(keyCode, scanCode, modifiers);
            return key || value;
        }

        @Override
        public boolean charTyped(char chr, int modifiers) {
            boolean key = keyElement.charTyped(chr, modifiers);
            boolean value = valueElement.charTyped(chr, modifiers);
            return key || value;
        }

        @Override
        protected int getHoveredControlWidth() {
            return getUnhoveredControlWidth();
        }

        @Override
        public void setDimension(Dimension<Integer> dim) {
            Dimension<Integer> keyDimension = dim.withWidth((int)((double)dim.width() * ratio));
            Dimension<Integer> valueDimension = dim.moved(keyDimension.width(), 0).withWidth((int)((double)dim.width() - keyDimension.width()));
            keyElement.setDimension(keyDimension);
            valueElement.setDimension(valueDimension);
            super.setDimension(dim);
        }

        @Override
        public void unfocus() {
            keyElement.unfocus();
            valueElement.unfocus();
            super.unfocus();
        }

        @Override
        public void render(DrawContext graphics, int mouseX, int mouseY, float delta) {
            keyElement.render(graphics, mouseX, mouseY, delta);
            valueElement.render(graphics, mouseX, mouseY, delta);
        }
    }

    public static class KeyValuePair<K, V> {
        private final K key;
        private final V value;

        public KeyValuePair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof KeyValuePair<?, ?> other))
                return false;
            return this.key.equals(other.key) && this.value.equals(other.value);
        }

        public static class KeyValueTypeAdapter implements JsonSerializer<KeyValuePair<?, ?>>, JsonDeserializer<KeyValuePair<?, ?>> {
            @Override
            public KeyValuePair<?, ?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
                JsonObject object = jsonElement.getAsJsonObject();
                JsonElement key = object.get("key");
                JsonElement value = object.get("value");
                Type[] typeArgs = ((ParameterizedType)type).getActualTypeArguments();
                return new KeyValuePair<>(context.deserialize(key, typeArgs[0]), context.deserialize(value, typeArgs[1]));
            }

            @Override
            public JsonElement serialize(KeyValuePair<?, ?> pair, Type type, JsonSerializationContext context) {
                JsonObject result = new JsonObject();
                result.add("key", context.serialize(pair.key));
                result.add("value", context.serialize(pair.value));
                return result;
            }
        }
    }
}
