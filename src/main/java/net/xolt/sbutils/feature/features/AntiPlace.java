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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.command.CommandHelper;
import net.xolt.sbutils.feature.Feature;
import net.xolt.sbutils.util.ChatUtils;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static net.xolt.sbutils.SbUtils.MC;

public class AntiPlace extends Feature {

    private static final String COMMAND = "antiplace";
    private static final String ALIAS = "noplace";

    private long lastMessageSentAt;

    @Override
    public void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        final LiteralCommandNode<FabricClientCommandSource> antiPlaceNode = dispatcher.register(ClientCommandManager.literal(COMMAND)
                .then(CommandHelper.bool("heads", "antiPlace.heads", () -> ModConfig.HANDLER.instance().antiPlace.heads, (value) -> ModConfig.HANDLER.instance().antiPlace.heads = value))
                .then(CommandHelper.bool("grass", "antiPlace.grass", () -> ModConfig.HANDLER.instance().antiPlace.grass, (value) -> ModConfig.HANDLER.instance().antiPlace.grass = value))
        );

        dispatcher.register(ClientCommandManager.literal(ALIAS)
                .executes(context ->
                        dispatcher.execute(COMMAND, context.getSource())
                )
                .redirect(antiPlaceNode));
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
        if (MC.level == null) {
            return false;
        }

        InteractionResult actionResult = MC.level.getBlockState(hitResult.getBlockPos()).use(MC.level, player, hand, hitResult);
        if ((actionResult == InteractionResult.CONSUME || actionResult == InteractionResult.SUCCESS) && !player.isShiftKeyDown()) {
            return false;
        }

        ItemStack held = player.getItemInHand(hand);
        if (ModConfig.HANDLER.instance().antiPlace.heads && isNamedHead(held))
            return true;
        if (ModConfig.HANDLER.instance().antiPlace.grass && isGrass(held))
            return true;

        return false;
    }

    private static boolean isNamedHead(ItemStack item) {
        return item.getItem().equals(Items.PLAYER_HEAD) && item.hasCustomHoverName();
    }

    private static boolean isGrass(ItemStack item) {
        return item.getItem().equals(Items.GRASS_BLOCK);
    }


}
