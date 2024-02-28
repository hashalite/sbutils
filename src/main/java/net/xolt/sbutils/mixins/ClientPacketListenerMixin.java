package net.xolt.sbutils.mixins;

import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.*;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.feature.features.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;

import static net.xolt.sbutils.SbUtils.MC;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "handleLogin", at = @At("TAIL"))
    private void onGameJoin(ClientboundLoginPacket packet, CallbackInfo ci) {
        SbUtils.SERVER_DETECTOR.onJoinGame();
        SbUtils.FEATURES.get(AutoAdvert.class).onJoinGame();
        SbUtils.FEATURES.get(JoinCommands.class).onJoinGame();
        SbUtils.FEATURES.get(AutoRaffle.class).onJoinGame();
        SbUtils.FEATURES.get(AutoKit.class).onJoinGame();
        SbUtils.FEATURES.get(AutoFix.class).onJoinGame();
    }

    @Inject(method = "handleContainerClose", at = @At("HEAD"))
    private void onCloseScreen(ClientboundContainerClosePacket packet, CallbackInfo ci) {
        SbUtils.FEATURES.get(AutoCrate.class).onServerCloseScreen();
    }

    @Inject(method = "handlePlayerInfoRemove", at = @At(value = "INVOKE", target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onPlayerRemove(ClientboundPlayerInfoRemovePacket packet, CallbackInfo ci, Iterator<UUID> var2, UUID uUID, PlayerInfo playerListEntry) {
        SbUtils.FEATURES.get(StaffDetector.class).onPlayerLeave(playerListEntry);
    }

    @Inject(method = "handlePlayerInfoRemove", at = @At("TAIL"))
    private void onPlayerRemoveTail(ClientboundPlayerInfoRemovePacket packet, CallbackInfo ci) {
        SbUtils.FEATURES.get(StaffDetector.class).afterPlayerLeave();
    }

    @Inject(method = "applyPlayerInfoUpdate", at = @At("HEAD"))
    private void onHandlePlayerListAction(ClientboundPlayerInfoUpdatePacket.Action action, ClientboundPlayerInfoUpdatePacket.Entry receivedEntry, PlayerInfo currentEntry, CallbackInfo ci) {
        if (action != ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED) {
            return;
        }

        if (receivedEntry.listed()) {
            StaffDetector.onPlayerJoin(currentEntry);
        } else {
            SbUtils.FEATURES.get(StaffDetector.class).onPlayerLeave(currentEntry);
        }
    }

    @Inject(method = "applyPlayerInfoUpdate", at = @At("TAIL"))
    private void onHandlePlayerListActionTail(ClientboundPlayerInfoUpdatePacket.Action action, ClientboundPlayerInfoUpdatePacket.Entry receivedEntry, PlayerInfo currentEntry, CallbackInfo ci) {
        if (action != ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED) {
            return;
        }

        if (!receivedEntry.listed()) {
            SbUtils.FEATURES.get(StaffDetector.class).afterPlayerLeave();
        }
    }

    @Inject(method = "handleTabListCustomisation", at = @At("HEAD"))
    private void onPlayerListHeader(ClientboundTabListPacket packet, CallbackInfo ci) {
        SbUtils.SERVER_DETECTOR.onPlayerListHeader(packet.getHeader().getString());
    }

    @Inject(method = "handleOpenSignEditor", at = @At("HEAD"), cancellable = true)
    private void onSignEditorOpen(ClientboundOpenSignEditorPacket packet, CallbackInfo ci) {
        if (AutoPrivate.onSignEditorOpen(packet)) {
            ci.cancel();
        }
    }

    @Inject(method = "handleContainerSetData", at = @At("HEAD"))
    private void onScreenHandlerPropertyUpdate(ClientboundContainerSetDataPacket packet, CallbackInfo ci) {
        if (MC.screen instanceof EnchantmentScreen && ((EnchantmentScreen) MC.screen).getMenu().containerId == packet.getContainerId()) {
            SbUtils.FEATURES.get(AutoSilk.class).onEnchantUpdate();
        }
    }

    @Inject(method = "handleContainerSetSlot", at = @At("TAIL"))
    private void onScreenHandlerSlotUpdate(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        SbUtils.FEATURES.get(AutoFix.class).onUpdateInventory();
        SbUtils.FEATURES.get(AutoKit.class).onUpdateInventory();
        SbUtils.FEATURES.get(AutoRaffle.class).onUpdateInventory();
        SbUtils.FEATURES.get(AutoSilk.class).onUpdateInvSlot(packet);
    }

    @Inject(method = "handleCommands", at = @At("TAIL"))
    private void afterCommandTree(ClientboundCommandsPacket packet, CallbackInfo ci) {
        SbUtils.SERVER_DETECTOR.afterCommandTree();
    }

    @Inject(method = "handleSetTime", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/util/thread/BlockableEventLoop;)V", shift = At.Shift.AFTER))
    private void afterSetTime(ClientboundSetTimePacket packet, CallbackInfo ci) {
        SbUtils.TPS_ESTIMATOR.onSetTime();
    }
}
