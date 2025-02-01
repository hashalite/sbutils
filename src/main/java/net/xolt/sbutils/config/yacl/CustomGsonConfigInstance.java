//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.xolt.sbutils.config.yacl;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import dev.isxander.yacl.config.ConfigEntry;
import dev.isxander.yacl.config.ConfigInstance;
import dev.isxander.yacl.impl.utils.YACLConstants;
import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.UnaryOperator;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class CustomGsonConfigInstance<T> extends ConfigInstance<T> {
    private final Gson gson;
    private final Path path;

    public CustomGsonConfigInstance(Class<T> configClass, Path path) {
        this(configClass, path, new GsonBuilder());
    }

    public CustomGsonConfigInstance(Class<T> configClass, Path path, Gson gson) {
        this(configClass, path, gson.newBuilder());
    }

    public CustomGsonConfigInstance(Class<T> configClass, Path path, UnaryOperator<GsonBuilder> builder) {
        this(configClass, path, builder.apply(new GsonBuilder()));
    }

    public CustomGsonConfigInstance(Class<T> configClass, Path path, GsonBuilder builder) {
        super(configClass);
        this.path = path;
        this.gson = builder.setExclusionStrategies(new ConfigExclusionStrategy())
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeHierarchyAdapter(Component.class, new Component.Serializer())
                .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
                .registerTypeHierarchyAdapter(Color.class, new ColorTypeAdapter())
                .create();
    }

    public void save() {
        try {
            YACLConstants.LOGGER.info("Saving {}...", this.getConfigClass().getSimpleName());
            Files.writeString(this.path, this.gson.toJson(this.getConfig()), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void load() {
        try {
            if (Files.notExists(this.path, new LinkOption[0])) {
                this.save();
                return;
            }

            YACLConstants.LOGGER.info("Loading {}...", this.getConfigClass().getSimpleName());
            this.setConfig(this.gson.fromJson(Files.readString(this.path), this.getConfigClass()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Path getPath() {
        return this.path;
    }

    private static class ConfigExclusionStrategy implements ExclusionStrategy {
        private ConfigExclusionStrategy() {
        }

        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return fieldAttributes.getAnnotation(ConfigEntry.class) == null;
        }

        public boolean shouldSkipClass(Class<?> aClass) {
            return false;
        }
    }

    public static class ColorTypeAdapter implements JsonSerializer<Color>, JsonDeserializer<Color> {
        public ColorTypeAdapter() {
        }

        public Color deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return new Color(jsonElement.getAsInt(), true);
        }

        public JsonElement serialize(Color color, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(color.getRGB());
        }
    }
}