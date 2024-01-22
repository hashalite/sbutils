package net.xolt.sbutils.config;

import com.google.gson.*;
import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyValueController<K, V> implements Controller<KeyValueController.KeyValuePair<K, V>> {
    private final Option<KeyValuePair<K, V>> option;
    private final double ratio;
    private final Controller<K> keyController;
    private final Controller<V> valueController;

    public KeyValueController(Option<KeyValuePair<K, V>> option, double ratio, Controller<K> keyController, Controller<V> valueController) {
        this.option = option;
        this.ratio = ratio;
        this.keyController = keyController;
        this.valueController = valueController;
    }

    @Override
    public Option<KeyValuePair<K, V>> option() {
        return option;
    }

    @Override
    public Text formatValue() {
        KeyValuePair<K, V> pair = option.pendingValue();
        return Text.literal("Key: " + pair.key + " -- Value: " + pair.value);
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

        public static class KeyValueTypeAdapter implements JsonSerializer<KeyValuePair<?, ?>>, JsonDeserializer<KeyValuePair<?, ?>> {
            @Override
            public KeyValuePair<?, ?> deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
                JsonObject object = jsonElement.getAsJsonObject();
                JsonElement key = object.get("key");
                JsonElement value = object.get("value");
                List<String> typeParameters = getTypeParameters(type);
                return new KeyValuePair<>(getObject(key, typeParameters.get(0), context), getObject(value, typeParameters.get(1), context));
            }

            @Override
            public JsonElement serialize(KeyValuePair<?, ?> pair, Type type, JsonSerializationContext context) {
                JsonObject result = new JsonObject();
                addProperty(result, "key", pair.getKey(), context);
                addProperty(result, "value", pair.getValue(), context);
                return result;
            }

            private List<String> getTypeParameters(Type type) {
                String typeString = type.getTypeName();
                return getTypeParameters(typeString);

            }

            private List<String> getTypeParameters(String typeString) {
                typeString = typeString.replaceFirst("net\\.xolt\\.sbutils\\.config\\.KeyValueController\\$KeyValuePair<", "");
                typeString = typeString.substring(0, typeString.length() - 1);
                Pattern paramPattern = Pattern.compile("java\\.lang\\.[a-zA-Z]+|net\\.xolt\\.sbutils\\.config\\.KeyValueController\\$KeyValuePair<.*>");
                Matcher matcher = paramPattern.matcher(typeString);
                List<String> result = new ArrayList<>();
                while (matcher.find()) {
                    result.add(matcher.group(0));
                }
                return result;
            }

            private Object getObject(JsonElement element, String type, JsonDeserializationContext context) {
                if (element.isJsonPrimitive()) {
                    JsonPrimitive primitive = (JsonPrimitive) element;
                    switch (type) {
                        case "java.lang.String":
                            return primitive.getAsString();
                        case "java.lang.Double":
                            return primitive.getAsDouble();
                        case "java.lang.Float":
                            return primitive.getAsFloat();
                        case "java.lang.Long":
                            return primitive.getAsLong();
                        case "java.lang.Integer":
                            return primitive.getAsInt();
                        case "java.lang.Boolean":
                            return primitive.getAsBoolean();
                    }
                } else if (element.isJsonObject()) {
                    JsonObject object = element.getAsJsonObject();
                    if (type.startsWith("net.xolt.sbutils.config.KeyValueController$KeyValuePair<")) {
                        List<String> typeParams = getTypeParameters(type);
                        return new KeyValuePair<>(getObject(object.get("key"), typeParams.get(0), context), getObject(object.get("value"), typeParams.get(1), context));
                    }
                }
                return null;
            }

            private void addProperty(JsonObject object, String property, Object value, JsonSerializationContext jsonSerializationContext) {
                if (value instanceof String) {
                    object.addProperty(property, (String) value);
                } else if (value instanceof Number) {
                    object.addProperty(property, (Number) value);
                } else if (value instanceof Boolean) {
                    object.addProperty(property, (Boolean) value);
                } else {
                    object.add(property, jsonSerializationContext.serialize(value));
                }
            }
        }
    }
}
