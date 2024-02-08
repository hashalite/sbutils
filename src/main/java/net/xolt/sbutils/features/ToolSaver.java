package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.util.CommandUtils;
import net.xolt.sbutils.util.Messenger;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class ToolSaver {

    private static final String COMMAND = "toolsaver";
    private static final String ALIAS = "saver";

    private static long lastMessageSentAt;

    public static void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        SbUtils.commands.addAll(List.of(COMMAND, ALIAS));
        final LiteralCommandNode<FabricClientCommandSource> toolSaverNode = dispatcher.register(
                CommandUtils.toggle(COMMAND, "toolSaver", () -> ModConfig.HANDLER.instance().toolSaver.enabled, (value) -> ModConfig.HANDLER.instance().toolSaver.enabled = value)
                    .then(CommandUtils.integer("durability", "durability", "toolSaver.durability", () -> ModConfig.HANDLER.instance().toolSaver.durability, (value) -> ModConfig.HANDLER.instance().toolSaver.durability = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                    dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(toolSaverNode));
    }

    public static boolean shouldCancelBlockInteract(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        if (!ModConfig.HANDLER.instance().toolSaver.enabled) {
            return false;
        }

        ItemStack item = player.getStackInHand(hand);
        if (!hasLowDurability(item)) {
            return false;
        }

        ItemUsageContext itemUsageContext = new ItemUsageContext(player, hand, hitResult);
        if (item.isDamageable() && item.getItem().useOnBlock(itemUsageContext) != ActionResult.PASS) {
            notifyBlocked();
            return true;
        }

        return false;
    }

    public static boolean shouldCancelEntityInteract(PlayerEntity player, Entity entity, Hand hand) {
        if (!ModConfig.HANDLER.instance().toolSaver.enabled) {
            return false;
        }

        ItemStack item = player.getStackInHand(hand);
        if (!hasLowDurability(item)) {
            return false;
        }

        if (item.getItem().equals(Items.SHEARS) &&
                (entity instanceof SheepEntity || entity instanceof MooshroomEntity || entity instanceof SnowGolemEntity)) {
            notifyBlocked();
            return true;
        }

        if (item.getItem().equals(Items.FLINT_AND_STEEL) &&
                (entity instanceof CreeperEntity || entity instanceof TntMinecartEntity)) {
            notifyBlocked();
            return true;
        }

        return false;
    }

    public static boolean shouldCancelAttack() {
        if (!ModConfig.HANDLER.instance().toolSaver.enabled || MC.player == null) {
            return false;
        }

        ItemStack holding = MC.player.getMainHandStack();

        if (hasLowDurability(holding)) {
            notifyBlocked();
            return true;
        }

        return false;
    }

    private static boolean hasLowDurability(ItemStack item) {
        if (item.isEmpty() || !item.isDamageable()) {
            return false;
        }

        return item.getMaxDamage() - item.getDamage() <= ModConfig.HANDLER.instance().toolSaver.durability;
    }

    private static void notifyBlocked() {
        if (System.currentTimeMillis() - lastMessageSentAt >= 5000) {
            Messenger.printMessage("message.sbutils.toolSaver.actionBlocked", Formatting.RED);
            lastMessageSentAt = System.currentTimeMillis();
        }
    }
}
