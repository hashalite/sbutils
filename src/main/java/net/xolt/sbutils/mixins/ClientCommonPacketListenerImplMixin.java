package net.xolt.sbutils.mixins;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.feature.features.AutoFix;
import net.xolt.sbutils.feature.features.AutoPrivate;
import net.xolt.sbutils.feature.features.ChatAppend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=1.20 {
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
//? } else {
/*import net.minecraft.network.Connection;
 *///? }

//? if >=1.20 {
@Mixin(ClientCommonPacketListenerImpl.class)
//? } else
//@Mixin(Connection.class)
public class ClientCommonPacketListenerImplMixin {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ServerboundContainerClickPacket) {
            SbUtils.FEATURES.get(AutoFix.class).onUpdateInventory();
        }
        //? if >=1.21.11 {
        if (packet instanceof ServerboundPlayerInputPacket) {
            SbUtils.FEATURES.get(AutoPrivate.class).onSendPlayerInput((ServerboundPlayerInputPacket) packet, ci);
        }
        //? } else {
        /*if (packet instanceof ServerboundPlayerCommandPacket) {
            SbUtils.FEATURES.get(AutoPrivate.class).onSendPlayerCommand((ServerboundPlayerCommandPacket) packet, ci);
        }
        *///? }
    }

    @ModifyVariable(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), argsOnly = true)
    private Packet<?> onSendPacket(Packet<?> packet) {
        if (packet instanceof ServerboundChatPacket) {
            return ChatAppend.processSentMessage((ServerboundChatPacket)packet);
        }
        return packet;
    }
}
