package net.xolt.sbutils.mixins;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.feature.features.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {

    @Inject(method = "handleDisconnection", at = @At("HEAD"))
    private void onHandleDisconnection(CallbackInfo ci) {
        SbUtils.SERVER_DETECTOR.onDisconnect();
        SbUtils.TPS_ESTIMATOR.onDisconnect();
        SbUtils.COMMAND_SENDER.onDisconnect();
        SbUtils.FEATURES.get(EnchantAll.class).onDisconnect();
        SbUtils.FEATURES.get(JoinCommands.class).onDisconnect();
        SbUtils.FEATURES.get(AutoSilk.class).onDisconnect();
        SbUtils.FEATURES.get(AutoFix.class).onDisconnect();
        SbUtils.FEATURES.get(AutoMine.class).onDisconnect();
        SbUtils.FEATURES.get(AutoKit.class).onDisconnect();

        if (ModConfig.HANDLER.getConfig().autoSilk.enabled || ModConfig.HANDLER.getConfig().autoCrate.enabled || ModConfig.HANDLER.getConfig().autoMine.enabled) {
            ModConfig.HANDLER.getConfig().autoSilk.enabled = false;
            ModConfig.HANDLER.getConfig().autoCrate.enabled = false;
            ModConfig.HANDLER.getConfig().autoMine.enabled = false;
            ModConfig.HANDLER.save();
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"))
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ServerboundContainerClickPacket) {
            SbUtils.FEATURES.get(AutoFix.class).onUpdateInventory();
        }
    }

    @ModifyVariable(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), argsOnly = true)
    private Packet<?> onSendPacket(Packet<?> packet) {
        if (packet instanceof ServerboundChatPacket) {
            return ChatAppend.processSentMessage((ServerboundChatPacket)packet);
        }
        return packet;
    }
}
