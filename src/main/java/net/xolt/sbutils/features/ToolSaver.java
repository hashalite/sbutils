package net.xolt.sbutils.features;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.Messenger;

import static net.xolt.sbutils.SbUtils.MC;

public class ToolSaver {

    private static long lastMessageSentAt;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralCommandNode<FabricClientCommandSource> toolSaverNode = dispatcher.register(ClientCommandManager.literal("toolsaver")
                .executes(context -> {
                    ModConfig.INSTANCE.getConfig().toolSaver = !ModConfig.INSTANCE.getConfig().toolSaver;
                    ModConfig.INSTANCE.save();
                    Messenger.printChangedSetting("text.sbutils.config.category.toolsaver", ModConfig.INSTANCE.getConfig().toolSaver);
                    return Command.SINGLE_SUCCESS;
                })
                .then(ClientCommandManager.literal("durability")
                        .executes(context -> {
                            Messenger.printSetting("text.sbutils.config.option.toolSaverDurability", ModConfig.INSTANCE.getConfig().toolSaverDurability);
                            return Command.SINGLE_SUCCESS;
                        })
                        .then(ClientCommandManager.argument("durability", IntegerArgumentType.integer())
                                .executes(context -> {
                                    ModConfig.INSTANCE.getConfig().toolSaverDurability = IntegerArgumentType.getInteger(context, "durability");
                                    ModConfig.INSTANCE.save();
                                    Messenger.printChangedSetting("text.sbutils.config.option.toolSaverDurability", ModConfig.INSTANCE.getConfig().toolSaverDurability);
                                    return Command.SINGLE_SUCCESS;
                                }))));

        dispatcher.register(ClientCommandManager.literal("saver")
                .executes(context ->
                    dispatcher.execute("toolsaver", context.getSource())
                )
                .redirect(toolSaverNode));
    }

    public static boolean onHandleBlockBreaking() {
        if (shouldCancelAction()) {
            if (System.currentTimeMillis() - lastMessageSentAt >= 5000) {
                Messenger.printMessage("message.sbutils.toolSaver.actionBlocked", Formatting.RED);
                lastMessageSentAt = System.currentTimeMillis();
            }
            return true;
        }
        return false;
    }

    public static boolean shouldCancelAction() {
        if (!ModConfig.INSTANCE.getConfig().toolSaver || MC.player == null) {
            return false;
        }

        ItemStack holding = MC.player.getMainHandStack();
        if (holding.isEmpty() || !holding.isDamageable()) {
            return false;
        }

        if (holding.getMaxDamage() - holding.getDamage() > ModConfig.INSTANCE.getConfig().toolSaverDurability) {
            return false;
        }

        return true;
    }

    public static void reset() {
        lastMessageSentAt = 0;
    }
}
