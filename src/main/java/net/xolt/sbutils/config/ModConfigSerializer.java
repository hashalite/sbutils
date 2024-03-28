package net.xolt.sbutils.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.ConfigSerializer;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Jankson;
import net.minecraft.client.MinecraftClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfigSerializer implements ConfigSerializer<ModConfig> {

    private final Class<ModConfig> configClass;
    private final Config definition;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    ModConfigSerializer(Config definition, Class<ModConfig> configClass) {
        this.definition = definition;
        this.configClass = configClass;
    }

    private Path getConfigPath() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve("sbutils").resolve(this.definition.name() + ".json");
    }

    @Override
    public void serialize(ModConfig config) throws ConfigSerializer.SerializationException {
        Path configPath = this.getConfigPath();
        try {
            Files.createDirectories(configPath.getParent());
            BufferedWriter writer = Files.newBufferedWriter(configPath);
            this.gson.toJson(config, writer);
            writer.close();
        } catch (IOException e) {
            throw new ConfigSerializer.SerializationException(e);
        }
    }

    public ModConfig deserialize() throws ConfigSerializer.SerializationException {
        Path configPath = this.getConfigPath();
        if (Files.exists(configPath)) {
            try {
                BufferedReader reader = Files.newBufferedReader(configPath);
                ModConfig ret = this.gson.fromJson(reader, this.configClass);
                reader.close();
                return ret;
            } catch (Throwable t) {
                throw new ConfigSerializer.SerializationException(t);
            }
        } else {
            return this.createDefault();
        }
    }

    public ModConfig createDefault() {
        return Utils.constructUnsafely(configClass);
    }
}
