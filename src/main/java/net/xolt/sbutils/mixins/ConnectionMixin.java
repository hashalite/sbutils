package net.xolt.sbutils.mixins;

import net.minecraft.network.Connection;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.feature.features.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {

    @Inject(method = "handleDisconnection", at = @At("HEAD"))
    private void onHandleDisconnection(CallbackInfo ci) {
        SbUtils.SERVER_DETECTOR.onDisconnect();
        SbUtils.TPS_ESTIMATOR.onDisconnect();
        SbUtils.FEATURES.get(EnchantAll.class).onDisconnect();
        SbUtils.FEATURES.get(JoinCommands.class).onDisconnect();
        SbUtils.FEATURES.get(AutoSilk.class).onDisconnect();
        SbUtils.FEATURES.get(AutoFix.class).onDisconnect();
        SbUtils.FEATURES.get(AutoMine.class).onDisconnect();
        SbUtils.FEATURES.get(AutoKit.class).onDisconnect();

        if (ModConfig.HANDLER.instance().autoSilk.enabled || ModConfig.HANDLER.instance().autoCrate.enabled || ModConfig.HANDLER.instance().autoMine.enabled) {
            ModConfig.HANDLER.instance().autoSilk.enabled = false;
            ModConfig.HANDLER.instance().autoCrate.enabled = false;
            ModConfig.HANDLER.instance().autoMine.enabled = false;
            ModConfig.HANDLER.save();
        }
    }
}
