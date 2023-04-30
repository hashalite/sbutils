package net.xolt.sbutils.mixins;

import net.minecraft.client.network.ClientPlayerEntity;
import net.xolt.sbutils.features.AutoSilk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "closeHandledScreen", at = @At("HEAD"))
    private void onCloseHandledScreen(CallbackInfo ci) {
        AutoSilk.onPlayerCloseScreen();
    }
}
