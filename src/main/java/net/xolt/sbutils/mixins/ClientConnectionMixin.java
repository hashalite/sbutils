package net.xolt.sbutils.mixins;

import net.minecraft.network.ClientConnection;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.*;
import net.xolt.sbutils.features.common.ServerDetector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    @Inject(method = "handleDisconnection", at = @At("HEAD"))
    private void onHandleDisconnection(CallbackInfo ci) {
        ServerDetector.onDisconnect();
        EnchantAll.onDisconnect();
        JoinCommands.onDisconnect();
        AutoSilk.onDisconnect();
        AutoFix.onDisconnect();
        AutoMine.onDisconnect();
        AutoKit.onDisconnect();

        if (ModConfig.HANDLER.instance().autoSilk.enabled || ModConfig.HANDLER.instance().autoCrate.enabled || ModConfig.HANDLER.instance().autoMine.enabled) {
            ModConfig.HANDLER.instance().autoSilk.enabled = false;
            ModConfig.HANDLER.instance().autoCrate.enabled = false;
            ModConfig.HANDLER.instance().autoMine.enabled = false;
            ModConfig.HANDLER.save();
        }
    }
}
