package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
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
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.ChatUtils;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class ToolSaver extends Feature {
    private final OptionBinding<Boolean> enabled = new OptionBinding<>("toolSaver.enabled", Boolean.class, (config) -> config.toolSaver.enabled, (config, value) -> config.toolSaver.enabled = value);
    private final OptionBinding<Integer> durability = new OptionBinding<>("toolSaver.durability", Integer.class, (config) -> config.toolSaver.durability, (config, value) -> config.toolSaver.durability = value);

    private long lastMessageSentAt;

    public ToolSaver() {
        super("toolSaver", "toolsaver", "saver");
    }

    @Override
    public List<? extends ConfigBinding<?>> getConfigBindings() {
        return List.of(enabled, durability);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> toolSaverNode = dispatcher.register(
                CommandHelper.toggle(command, this, enabled)
                    .then(CommandHelper.integer("durability", "durability", durability))
        );
        registerAlias(dispatcher, toolSaverNode);
    }

    public void onBlockInteract(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (shouldCancelBlockInteract(player, hand, hitResult)) {
            cir.setReturnValue(InteractionResult.PASS);
            notifyBlocked();
        }
    }

    public void onEntityInteract(Player player, Entity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (shouldCancelEntityInteract(player, entity, hand)) {
            cir.setReturnValue(InteractionResult.PASS);
            notifyBlocked();
        }
    }

    public void onContinueAttack(boolean breaking, CallbackInfo ci) {
        if (breaking && shouldCancelAttack()) {
            ci.cancel();
            notifyBlocked();
        }
    }

    public void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        if (shouldCancelAttack()) {
            cir.cancel();
            notifyBlocked();
        }
    }

    private void notifyBlocked() {
        if (System.currentTimeMillis() - lastMessageSentAt >= 5000) {
            ChatUtils.printMessage("message.sbutils.toolSaver.actionBlocked", ChatFormatting.RED);
            lastMessageSentAt = System.currentTimeMillis();
        }
    }

    private static boolean shouldCancelBlockInteract(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult) {
        if (!ModConfig.HANDLER.instance().toolSaver.enabled)
            return false;

        ItemStack item = player.getItemInHand(hand);
        if (!hasLowDurability(item))
            return false;

        UseOnContext itemUsageContext = new UseOnContext(player, hand, hitResult);
        if (item.isDamageableItem() && item.getItem().useOn(itemUsageContext) != InteractionResult.PASS)
            return true;

        return false;
    }

    private static boolean shouldCancelEntityInteract(Player player, Entity entity, InteractionHand hand) {
        if (!ModConfig.HANDLER.instance().toolSaver.enabled)
            return false;

        ItemStack item = player.getItemInHand(hand);
        if (!hasLowDurability(item))
            return false;

        if (item.getItem().equals(Items.SHEARS) &&
                (entity instanceof Sheep || entity instanceof MushroomCow || entity instanceof SnowGolem))
            return true;

        if (item.getItem().equals(Items.FLINT_AND_STEEL) &&
                (entity instanceof Creeper || entity instanceof MinecartTNT))
            return true;

        return false;
    }

    public static boolean shouldCancelAttack() {
        if (!ModConfig.HANDLER.instance().toolSaver.enabled || MC.player == null)
            return false;

        ItemStack holding = MC.player.getMainHandItem();

        if (hasLowDurability(holding))
            return true;

        return false;
    }

    private static boolean hasLowDurability(ItemStack item) {
        if (item.isEmpty() || !item.isDamageableItem())
            return false;

        return item.getMaxDamage() - item.getDamageValue() <= ModConfig.HANDLER.instance().toolSaver.durability;
    }
}
