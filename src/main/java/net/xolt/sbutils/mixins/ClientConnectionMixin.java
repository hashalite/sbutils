package net.xolt.sbutils.mixins;

import net.minecraft.network.ClientConnection;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    @Inject(method = "handleDisconnection", at = @At("HEAD"))
    private void onHandleDisconnection(CallbackInfo ci) {
        EnchantAll.reset();
        JoinCommands.reset();
        AutoSilk.reset();
        ToolSaver.reset();
        AutoFix.reset();
        AutoCrate.reset();

        if (ModConfig.INSTANCE.getConfig().autoSilk || ModConfig.INSTANCE.getConfig().autoCrate || ModConfig.INSTANCE.getConfig().autoMine) {
            ModConfig.INSTANCE.getConfig().autoSilk = false;
            ModConfig.INSTANCE.getConfig().autoCrate = false;
            ModConfig.INSTANCE.getConfig().autoMine = false;
            ModConfig.INSTANCE.save();
        }
    }
}
