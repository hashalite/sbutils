package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.Messenger;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class AntiPlace {

    private static final String COMMAND = "antiplace";
    private static final String ALIAS = "noplace";

    private static long lastMessageSentAt;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> antiPlaceNode = dispatcher.register(ClientCommandManager.literal(COMMAND)
                .then(ClientCommandManager.literal("heads")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.antiPlaceHeads", ModConfig.INSTANCE.getConfig().antiPlaceHeads);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().antiPlaceHeads = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.antiPlaceHeads", ModConfig.INSTANCE.getConfig().antiPlaceHeads);
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(ClientCommandManager.literal("grass")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.antiPlaceGrass", ModConfig.INSTANCE.getConfig().antiPlaceGrass);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().antiPlaceGrass = BoolArgumentType.getBool(context, "enabled");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.antiPlaceGrass", ModConfig.INSTANCE.getConfig().antiPlaceGrass);
                                    return Command.SINGLE_SUCCESS;
                                }))));

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute("antiplace", context.getSource())
                )
                .redirect(antiPlaceNode));
    }

    public static boolean shouldCancelBlockInteract(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        if (MC.world == null) {
            return false;
        }

        ActionResult actionResult = MC.world.getBlockState(hitResult.getBlockPos()).onUse(MC.world, player, hand, hitResult);
        if ((actionResult == ActionResult.CONSUME || actionResult == ActionResult.SUCCESS) && !player.isSneaking()) {
            return false;
        }

        ItemStack held = player.getStackInHand(hand);
        if (ModConfig.INSTANCE.getConfig().antiPlaceHeads && isNamedHead(held)) {
            notifyBlocked("message.sbutils.antiPlace.headBlocked");
            return true;
        }

        if (ModConfig.INSTANCE.getConfig().antiPlaceGrass && isGrass(held)) {
            notifyBlocked("message.sbutils.antiPlace.grassBlocked");
            return true;
        }

        return false;
    }

    private static boolean isNamedHead(ItemStack item) {
        return item.getItem().equals(Items.PLAYER_HEAD) && item.hasCustomName();
    }

    private static boolean isGrass(ItemStack item) {
        return item.getItem().equals(Items.GRASS_BLOCK);
    }

    private static void notifyBlocked(String message) {
        if (System.currentTimeMillis() - lastMessageSentAt >= 5000) {
            Messenger.printMessage(message, Formatting.RED);
            lastMessageSentAt = System.currentTimeMillis();
        }
    }
}
