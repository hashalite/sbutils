package net.xolt.sbutils.mixins;

import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.network.SocialInteractionsManager;
import net.xolt.sbutils.features.StaffDetector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(SocialInteractionsManager.class)
public class SocialInteractionsManagerMixin {

    @Inject(method = "setPlayerOnline", at = @At("HEAD"))
    private void onPlayerJoin(PlayerListEntry player, CallbackInfo ci) {
        StaffDetector.onPlayerJoin(player);
    }

    @Inject(method = "setPlayerOffline", at = @At("HEAD"))
    private void onPlayerLeave(UUID uuid, CallbackInfo ci) {
        StaffDetector.onPlayerLeave(uuid);
    }
}
