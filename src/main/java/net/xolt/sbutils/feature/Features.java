package net.xolt.sbutils.feature;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;

import java.util.*;

public class Features {

    private final Map<Class<? extends Feature>, Feature> features = new IdentityHashMap<>();
    private final List<Feature> ordered = new ArrayList<>();

    @SafeVarargs
    public final <T extends Feature> void add(T... features) {
        for (Feature feature : features) {
            this.features.put(feature.getClass(), feature);
            this.ordered.add(feature);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Feature> T get(Class<T> feature) {
        return (T) features.get(feature);
    }

    public Collection<Feature> getAll() {
        return ordered;
    }

    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        ordered.forEach((feature) -> feature.registerCommands(dispatcher, registryAccess));
    }
}
