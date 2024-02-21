package net.xolt.sbutils.mixins;

import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.xolt.sbutils.features.*;
import net.xolt.sbutils.features.common.ServerDetector;
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
        ServerDetector.onJoinGame();
        AutoAdvert.onJoinGame();
        JoinCommands.onJoinGame();
        AutoRaffle.onJoinGame();
        AutoKit.onJoinGame();
        AutoFix.onJoinGame();
    }

    @Inject(method = "handleContainerClose", at = @At("HEAD"))
    private void onCloseScreen(ClientboundContainerClosePacket packet, CallbackInfo ci) {
        AutoCrate.onServerCloseScreen();
    }

    @Inject(method = "handlePlayerInfoRemove", at = @At(value = "INVOKE", target = "Ljava/util/Set;remove(Ljava/lang/Object;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void onPlayerRemove(ClientboundPlayerInfoRemovePacket packet, CallbackInfo ci, Iterator var2, UUID uUID, PlayerInfo playerListEntry) {
        StaffDetector.onPlayerLeave(playerListEntry);
    }

    @Inject(method = "handlePlayerInfoRemove", at = @At("TAIL"))
    private void onPlayerRemoveTail(ClientboundPlayerInfoRemovePacket packet, CallbackInfo ci) {
        StaffDetector.afterPlayerLeave();
    }

    @Inject(method = "applyPlayerInfoUpdate", at = @At("HEAD"))
    private void onHandlePlayerListAction(ClientboundPlayerInfoUpdatePacket.Action action, ClientboundPlayerInfoUpdatePacket.Entry receivedEntry, PlayerInfo currentEntry, CallbackInfo ci) {
        if (action != ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED) {
            return;
        }

        if (receivedEntry.listed()) {
            StaffDetector.onPlayerJoin(currentEntry);
        } else {
            StaffDetector.onPlayerLeave(currentEntry);
        }
    }

    @Inject(method = "applyPlayerInfoUpdate", at = @At("TAIL"))
    private void onHandlePlayerListActionTail(ClientboundPlayerInfoUpdatePacket.Action action, ClientboundPlayerInfoUpdatePacket.Entry receivedEntry, PlayerInfo currentEntry, CallbackInfo ci) {
        if (action != ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED) {
            return;
        }

        if (!receivedEntry.listed()) {
            StaffDetector.afterPlayerLeave();
        }
    }

    @Inject(method = "handleTabListCustomisation", at = @At("HEAD"))
    private void onPlayerListHeader(ClientboundTabListPacket packet, CallbackInfo ci) {
        ServerDetector.onPlayerListHeader(packet.getHeader().getString());
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
            AutoSilk.onEnchantUpdate();
        }
    }

    @Inject(method = "handleContainerSetSlot", at = @At("TAIL"))
    private void onScreenHandlerSlotUpdate(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        AutoFix.onUpdateInventory();
        AutoKit.onUpdateInventory();
        AutoRaffle.onUpdateInventory();
        AutoSilk.onUpdateInvSlot(packet);
    }

    @Inject(method = "handleCommands", at = @At("TAIL"))
    private void afterCommandTree(ClientboundCommandsPacket packet, CallbackInfo ci) {
        ServerDetector.afterCommandTree();
    }
}
