package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.systems.CommandSender;
import org.jetbrains.annotations.Nullable;
//? if >=1.19.4 {
import net.minecraft.core.registries.BuiltInRegistries;
//? } else {
/*import net.minecraft.core.Registry;
 *///? }

import java.util.List;

public class CommandAliases extends Feature<ModConfig> {


    public CommandAliases() {
        super("sbutils", "commandAliases", null, null);
    }

    @Override
    public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
        return null;
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        dispatcher.register(
                ClientCommandManager.literal("buying")
                        .executes(context -> findWanted())
                        .then(ClientCommandManager.argument("item", ItemArgument.item(registryAccess))
                                .executes(context -> findWanted(ItemArgument.getItem(context, "item"))))
        );

        dispatcher.register(
                ClientCommandManager.literal("selling")
                        .executes(context -> findOwned())
                        .then(ClientCommandManager.argument("item", ItemArgument.item(registryAccess))
                                .executes(context -> findOwned(ItemArgument.getItem(context, "item"))))
        );

    }

    private int findOwned() {
        return findOwned(null);
    }

    private int findWanted() {
        return findWanted(null);
    }

    private int findOwned(@Nullable ItemInput item) {
        return shopFind(item, true);
    }

    private int findWanted(@Nullable ItemInput item) {
        return shopFind(item, false);
    }

    private int shopFind(@Nullable ItemInput item, boolean owned) {
        String shopType = owned ? "owned" : "wanted";
        CommandSender.sendNow(
                "shops find " + shopType + (item == null ? "" : " " +
                        //? if >=1.19.4 {
                        BuiltInRegistries.ITEM.getKey(item.getItem()).getPath()
                        //? } else
                        //Registry.ITEM.getKey(item.getItem()).getPath()
                ));
        return Command.SINGLE_SUCCESS;
    }
}
