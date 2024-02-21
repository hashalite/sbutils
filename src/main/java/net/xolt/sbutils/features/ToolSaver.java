package net.xolt.sbutils.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
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
                CommandHelper.toggle(COMMAND, "toolSaver", () -> ModConfig.HANDLER.instance().toolSaver.enabled, (value) -> ModConfig.HANDLER.instance().toolSaver.enabled = value)
                    .then(CommandHelper.integer("durability", "durability", "toolSaver.durability", () -> ModConfig.HANDLER.instance().toolSaver.durability, (value) -> ModConfig.HANDLER.instance().toolSaver.durability = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                    dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(toolSaverNode));
    }

    public static boolean shouldCancelBlockInteract(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult) {
        if (!ModConfig.HANDLER.instance().toolSaver.enabled) {
            return false;
        }

        ItemStack item = player.getItemInHand(hand);
        if (!hasLowDurability(item)) {
            return false;
        }

        UseOnContext itemUsageContext = new UseOnContext(player, hand, hitResult);
        if (item.isDamageableItem() && item.getItem().useOn(itemUsageContext) != InteractionResult.PASS) {
            notifyBlocked();
            return true;
        }

        return false;
    }

    public static boolean shouldCancelEntityInteract(Player player, Entity entity, InteractionHand hand) {
        if (!ModConfig.HANDLER.instance().toolSaver.enabled) {
            return false;
        }

        ItemStack item = player.getItemInHand(hand);
        if (!hasLowDurability(item)) {
            return false;
        }

        if (item.getItem().equals(Items.SHEARS) &&
                (entity instanceof Sheep || entity instanceof MushroomCow || entity instanceof SnowGolem)) {
            notifyBlocked();
            return true;
        }

        if (item.getItem().equals(Items.FLINT_AND_STEEL) &&
                (entity instanceof Creeper || entity instanceof MinecartTNT)) {
            notifyBlocked();
            return true;
        }

        return false;
    }

    public static boolean shouldCancelAttack() {
        if (!ModConfig.HANDLER.instance().toolSaver.enabled || MC.player == null) {
            return false;
        }

        ItemStack holding = MC.player.getMainHandItem();

        if (hasLowDurability(holding)) {
            notifyBlocked();
            return true;
        }

        return false;
    }

    private static boolean hasLowDurability(ItemStack item) {
        if (item.isEmpty() || !item.isDamageableItem()) {
            return false;
        }

        return item.getMaxDamage() - item.getDamageValue() <= ModConfig.HANDLER.instance().toolSaver.durability;
    }

    private static void notifyBlocked() {
        if (System.currentTimeMillis() - lastMessageSentAt >= 5000) {
            Messenger.printMessage("message.sbutils.toolSaver.actionBlocked", ChatFormatting.RED);
            lastMessageSentAt = System.currentTimeMillis();
        }
    }
}
