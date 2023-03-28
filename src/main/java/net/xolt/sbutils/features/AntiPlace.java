package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.Messenger;

import static net.xolt.sbutils.SbUtils.MC;

public class AntiPlace {
    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralCommandNode<FabricClientCommandSource> antiPlaceNode = dispatcher.register(ClientCommandManager.literal("antiplace")
                .then(ClientCommandManager.literal("heads")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.antiPlaceHeads", ModConfig.INSTANCE.getConfig().antiPlaceHeads);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().antiPlaceHeads = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.antiPlaceHeads", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().antiPlaceHeads = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.antiPlaceHeads", false);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("grass")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.antiPlaceGrass", ModConfig.INSTANCE.getConfig().antiPlaceGrass);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.literal("true")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().antiPlaceGrass = true;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.antiPlaceGrass", true);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(ClientCommandManager.literal("false")
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().antiPlaceGrass = false;
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.antiPlaceGrass", false);
                                    return Command.SINGLE_SUCCESS;
                                }))));

        dispatcher.register(ClientCommandManager.literal("noplace")
                .executes(context ->
                        dispatcher.execute("antiplace", context.getSource())
                )
                .redirect(antiPlaceNode));
    }

    public static boolean onHandleBlockPlace() {
        if (ModConfig.INSTANCE.getConfig().antiPlaceHeads && isHoldingNamedHead()) {
            return true;
        }
        if (ModConfig.INSTANCE.getConfig().antiPlaceGrass && isHoldingGrass()) {
            return true;
        }
        return false;
    }

    private static boolean isHoldingNamedHead() {
        if (MC.player == null) {
            return false;
        }

        ItemStack held = MC.player.getMainHandStack();
        return held.getItem().equals(Items.PLAYER_HEAD) && held.hasCustomName();
    }

    private static boolean isHoldingGrass() {
        if (MC.player == null) {
            return false;
        }

        ItemStack held = MC.player.getMainHandStack();
        return held.getItem().equals(Items.GRASS_BLOCK);
    }
}
