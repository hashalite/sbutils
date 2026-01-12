package net.xolt.sbutils.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.feature.features.*;
import net.xolt.sbutils.util.DummyPacket;
import org.apache.commons.lang3.mutable.MutableObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Inject(method = "startPrediction", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;predict(I)Lnet/minecraft/network/protocol/Packet;"), cancellable = true)
    private void onPredict(ClientLevel level, PredictiveAction action, CallbackInfo ci, @Local LocalRef<Packet<ServerGamePacketListener>> localPacket) {
        if (localPacket.get() instanceof DummyPacket) {
            ci.cancel();
        }
    }

    @Inject(method = "method_41933", at = @At("RETURN"), cancellable = true)
    private void onPredictUseItemOn(MutableObject<InteractionResult> mutableObject, LocalPlayer localPlayer, InteractionHand interactionHand, BlockHitResult blockHitResult, int i, CallbackInfoReturnable<Packet<ServerGamePacketListener>> cir) {
        InteractionResult result = mutableObject
                //? if >=1.21.11 {
                .get();
                //? } else
                //.getValue();
        if (result == null) {
            cir.setReturnValue(new DummyPacket(interactionHand, blockHitResult, i));
        }
    }

    @Inject(method = "performUseItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/context/UseOnContext;<init>(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)V"), cancellable = true)
    private void onUseNotConsumed(LocalPlayer player, InteractionHand hand, BlockHitResult result, CallbackInfoReturnable<InteractionResult> cir) {
        SbUtils.FEATURES.get(ToolSaver.class).onBlockInteract(player, hand, result, cir);
        SbUtils.FEATURES.get(AntiPlace.class).onBlockInteract(player, hand, result, cir);
    }

    @Inject(method = "useItemOn", at = @At("HEAD"))
    private void onInteractBlock(LocalPlayer player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        SbUtils.FEATURES.get(AutoPrivate.class).onInteractBlock();
    }

    @Inject(method = "useItemOn", at = @At("TAIL"))
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
