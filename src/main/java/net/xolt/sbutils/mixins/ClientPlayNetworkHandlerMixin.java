package net.xolt.sbutils.mixins;

import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.xolt.sbutils.features.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.xolt.sbutils.SbUtils.MC;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        JoinCommands.onJoinGame();
        AutoAdvert.onJoinGame();
        AutoLottery.onGameJoin();
    }

    @Inject(method = "onCloseScreen", at = @At("HEAD"))
    private void onCloseScreen(CloseScreenS2CPacket packet, CallbackInfo ci) {
        AutoCrate.onServerCloseScreen();
    }

    @Inject(method = "onPlayerRemove", at = @At("TAIL"))
    private void afterPlayerLeave(PlayerRemoveS2CPacket packet, CallbackInfo ci) {
        StaffDetector.afterPlayerLeave();
    }

    @Inject(method = "onSignEditorOpen", at = @At("HEAD"), cancellable = true)
    private void onSignEditorOpen(SignEditorOpenS2CPacket packet, CallbackInfo ci) {
        if (AutoPrivate.onSignEditorOpen(packet)) {
            ci.cancel();
        }
    }

    @Inject(method = "onScreenHandlerPropertyUpdate", at = @At("HEAD"))
    private void onScreenHandlerPropertyUpdate(ScreenHandlerPropertyUpdateS2CPacket packet, CallbackInfo ci) {
        if (MC.currentScreen instanceof EnchantmentScreen && ((EnchantmentScreen) MC.currentScreen).getScreenHandler().syncId == packet.getSyncId()) {
            AutoSilk.onEnchantUpdate();
        }
    }

    @Inject(method = "onScreenHandlerSlotUpdate", at = @At("TAIL"))
    private void onScreenHandlerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
        AutoFix.onUpdateInventory();
        AutoSilk.onInventoryUpdate(packet);
    }

    @Inject(method = "sendPacket", at = @At("HEAD"))
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ClickSlotC2SPacket) {
            AutoFix.onUpdateInventory();
        }
    }

    @ModifyVariable(method = "sendPacket", at = @At("HEAD"), argsOnly = true)
    private Packet<?> onSendPacket(Packet<?> packet) {
        if (packet instanceof ChatMessageC2SPacket) {
            return ChatAppend.processSentMessage((ChatMessageC2SPacket)packet);
        }
        return packet;
    }
}
