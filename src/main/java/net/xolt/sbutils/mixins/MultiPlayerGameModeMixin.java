package net.xolt.sbutils.mixins;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.feature.features.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
    private void onInteractBlock(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        SbUtils.FEATURES.get(AutoPrivate.class).onInteractBlock();
        SbUtils.FEATURES.get(ToolSaver.class).onBlockInteract(player, hand, hitResult, cir);
        SbUtils.FEATURES.get(AntiPlace.class).onBlockInteract(player, hand, hitResult, cir);
    }

    @Inject(method = "useItemOn", at = @At("TAIL"), cancellable = true)
    private void afterInteractBlock(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        SbUtils.FEATURES.get(AutoPrivate.class).afterInteractBlock();
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void onInteractEntity(Player player, Entity entity, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        SbUtils.FEATURES.get(ToolSaver.class).onEntityInteract(player, entity, hand, cir);
    }

    @Inject(method = "interactAt", at = @At("HEAD"), cancellable = true)
    private void onInteractEntityAtLocation(Player player, Entity entity, EntityHitResult hitResult, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        SbUtils.FEATURES.get(ToolSaver.class).onEntityInteract(player, entity, hand, cir);
    }

    @Inject(method = "handleInventoryMouseClick", at = @At("TAIL"))
    private void afterClickSlot(int syncId, int slotId, int button, ClickType actionType, Player player, CallbackInfo ci) {
        SbUtils.FEATURES.get(AutoFix.class).onUpdateInventory();
        SbUtils.FEATURES.get(AutoKit.class).onUpdateInventory();
        SbUtils.FEATURES.get(AutoRaffle.class).onUpdateInventory();
    }
}
