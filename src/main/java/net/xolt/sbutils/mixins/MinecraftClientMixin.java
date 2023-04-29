package net.xolt.sbutils.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.Screen;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    public Screen currentScreen;

    @Shadow
    private Overlay overlay;

    @Shadow
    protected int attackCooldown;

    @Shadow
    protected abstract void handleBlockBreaking(boolean breaking);

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        EnchantAll.tick();
        AutoAdvert.tick();
        JoinCommands.tick();
        AutoCrate.tick();
        AutoSilk.tick();
        AutoFix.tick();
        AutoRaffle.tick();
        AutoReply.tick();
        AutoCommand.tick();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci) {
        AutoMine.tick();
        if (ModConfig.INSTANCE.getConfig().autoMine && AutoMine.shouldMine() && (currentScreen != null || overlay != null)) {
            attackCooldown = 0;
            handleBlockBreaking(true);
        }
    }

    @Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
    public void onHandleBlockBreaking(boolean breaking, CallbackInfo ci) {
        if (breaking && ToolSaver.shouldCancelAttack()) {
            ci.cancel();
        }
    }

    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    public void onDoAttack(CallbackInfoReturnable<Boolean> cir) {
        if (ToolSaver.shouldCancelAttack()) {
            cir.cancel();
        }
    }
}
