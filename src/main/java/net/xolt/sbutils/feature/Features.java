package net.xolt.sbutils.feature;

import com.mojang.brigadier.CommandDispatcher;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;

import java.util.*;

public class Features<C> {

    private final Map<Class<? extends Feature<C>>, Feature<C>> features = new IdentityHashMap<>();
    private final List<Feature<C>> ordered = new ArrayList<>();
    private final ConfigClassHandler<C> configHandler;

    public Features(ConfigClassHandler<C> configHandler) {
        this.configHandler = configHandler;
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    public final <T extends Feature<C>> void add(T... features) {
        for (Feature<C> feature : features) {
            this.features.put((Class<? extends Feature<C>>) feature.getClass(), feature);
            this.ordered.add(feature);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Feature<C>> T get(Class<T> feature) {
        return (T) features.get(feature);
    }

    public Collection<Feature<C>> getAll() {
        return ordered;
    }

    public ConfigClassHandler<C> getConfigHandler() {
        return configHandler;
    }

    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        ordered.forEach((feature) -> feature.registerCommands(dispatcher, registryAccess));
    }
}
