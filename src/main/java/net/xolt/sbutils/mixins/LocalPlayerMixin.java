package net.xolt.sbutils.mixins;

import net.minecraft.client.player.LocalPlayer;
import net.xolt.sbutils.features.AutoCrate;
import net.xolt.sbutils.features.AutoSilk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(method = "closeContainer", at = @At("HEAD"))
    private void onCloseContainer(CallbackInfo ci) {
        AutoCrate.onPlayerCloseScreen();
        AutoSilk.onPlayerCloseScreen();
    }
}
