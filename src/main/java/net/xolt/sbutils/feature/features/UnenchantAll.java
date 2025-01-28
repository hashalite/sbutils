package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.feature.Feature;

import java.util.List;

public class UnenchantAll extends EnchantAll {

    public UnenchantAll() {
        super("sbutils", "unenchantAll", "unenchall", "ueall");
    }

    @Override
    public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
        return null;
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> unenchantAllNode = dispatcher.register(
                CommandHelper.runnable(command, () -> SbUtils.FEATURES.get(EnchantAll.class).onEnchantAllCommand(true, false))
                        .then(CommandHelper.runnable("inv", () -> SbUtils.FEATURES.get(EnchantAll.class).onEnchantAllCommand(true, true)))
                        .then(CommandHelper.doubl("delay", "seconds", delay, ModConfig.HANDLER))
                        .then(CommandHelper.integer("cooldownFrequency", "frequency", cooldownFrequency, ModConfig.HANDLER))
                        .then(CommandHelper.doubl("cooldownTime", "seconds", cooldownTime, ModConfig.HANDLER))
                        .then(CommandHelper.bool("tpsSync", tpsSync, ModConfig.HANDLER))
        );
        registerAlias(dispatcher, unenchantAllNode);
    }
}
