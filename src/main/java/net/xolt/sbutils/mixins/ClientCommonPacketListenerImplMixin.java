package net.xolt.sbutils.mixins;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.xolt.sbutils.features.AutoFix;
import net.xolt.sbutils.features.ChatAppend;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public class ClientCommonPacketListenerImplMixin {

    @Inject(method = "send", at = @At("HEAD"))
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ServerboundContainerClickPacket) {
            AutoFix.onUpdateInventory();
        }
    }

    @ModifyVariable(method = "send", at = @At("HEAD"), argsOnly = true)
    private Packet<?> onSendPacket(Packet<?> packet) {
        if (packet instanceof ServerboundChatPacket) {
            return ChatAppend.processSentMessage((ServerboundChatPacket)packet);
        }
        return packet;
    }
}
