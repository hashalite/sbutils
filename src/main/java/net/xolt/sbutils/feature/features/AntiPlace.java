package net.xolt.sbutils.feature.features;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.mixin.item.client.ClientPlayerInteractionManagerMixin;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.config.binding.ConfigBinding;
import net.xolt.sbutils.config.binding.OptionBinding;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.ChatUtils;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class AntiPlace extends Feature<ModConfig> {
    private final OptionBinding<ModConfig, Boolean> heads = new OptionBinding<>("sbutils", "antiPlace.heads", Boolean.class, (config) -> config.antiPlace.heads, (config, value) -> config.antiPlace.heads = value);
    private final OptionBinding<ModConfig, Boolean> grass = new OptionBinding<>("sbutils", "antiPlace.grass", Boolean.class, (config) -> config.antiPlace.grass, (config, value) -> config.antiPlace.grass = value);

    private long lastMessageSentAt;

    public AntiPlace() {
        super("sbutils", "antiPlace", "antiplace", "noplace");
    }

    @Override
    public List<? extends ConfigBinding<ModConfig, ?>> getConfigBindings() {
        return List.of(heads, grass);
    }

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> antiPlaceNode = dispatcher.register(ClientCommandManager.literal(command)
                .then(CommandHelper.bool("heads", heads, ModConfig.HANDLER))
                .then(CommandHelper.bool("grass", grass, ModConfig.HANDLER))
        );
        registerAlias(dispatcher, antiPlaceNode);
    }

    private void notifyBlocked(String message) {
        if (System.currentTimeMillis() - lastMessageSentAt >= 5000) {
            ChatUtils.printMessage(message, ChatFormatting.RED);
            lastMessageSentAt = System.currentTimeMillis();
        }
    }

    public void onBlockInteract(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        if (shouldCancelBlockInteract(player, hand, hitResult)) {
            cir.setReturnValue(InteractionResult.PASS);
            notifyBlocked(isGrass(player.getItemInHand(hand)) ? "message.sbutils.antiPlace.grassBlocked" : "message.sbutils.antiPlace.headBlocked");
        }
    }

    private static boolean shouldCancelBlockInteract(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult) {
        assert MC.level != null;
        InteractionResult actionResult = MC.level.getBlockState(hitResult.getBlockPos()).useWithoutItem(MC.level, player, hitResult);

        if (actionResult.consumesAction() && !player.isShiftKeyDown())
            return false;

        ItemStack held = player.getItemInHand(hand);
        if (ModConfig.HANDLER.instance().antiPlace.heads && isNamedHead(held))
            return true;
        if (ModConfig.HANDLER.instance().antiPlace.grass && isGrass(held))
            return true;

        return false;
    }

    private static boolean isNamedHead(ItemStack item) {
        return item.getItem().equals(Items.PLAYER_HEAD) && item.getCustomName() != null;

    }

    private static boolean isGrass(ItemStack item) {
        return item.getItem().equals(Items.GRASS_BLOCK);
    }


}
