package net.xolt.sbutils.config;

import com.google.gson.*;
import dev.isxander.yacl.config.ConfigEntry;
import dev.isxander.yacl.config.ConfigInstance;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import dev.isxander.yacl.config.GsonConfigInstance;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.xolt.sbutils.SbUtils;

public class ModConfigInstance<T> extends ConfigInstance<T> {
    private final Gson gson;
    private final Path path;

    public ModConfigInstance(Class<T> configClass, Path path) {
        super(configClass);
        this.path = path;
        this.gson = new GsonBuilder()
                .setExclusionStrategies(new ConfigExclusionStrategy())
                .registerTypeHierarchyAdapter(Text.class, new Text.Serializer())
                .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
                .registerTypeHierarchyAdapter(Color.class, new GsonConfigInstance.ColorTypeAdapter())
                .serializeNulls()
                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .setPrettyPrinting()
                .create();
    }

    @Override
    public void save() {
        try {
            SbUtils.LOGGER.info("Saving sbutils config...");
            Files.writeString(path, gson.toJson(getConfig()), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load() {
        try {
            if (Files.notExists(path)) {
                save();
                return;
            }

            SbUtils.LOGGER.info("Loading sbutils config...");
            setConfig(gson.fromJson(Files.readString(path), getConfigClass()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ConfigExclusionStrategy implements ExclusionStrategy {
        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return fieldAttributes.getAnnotation(ConfigEntry.class) == null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> aClass) {
            return false;
        }
    }
}

