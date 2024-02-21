package net.xolt.sbutils.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.features.*;
import net.xolt.sbutils.features.common.InvCleaner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    public Screen screen;

    @Shadow
    private Overlay overlay;

    @Shadow
    protected int missTime;

    @Shadow
    protected abstract void continueAttack(boolean breaking);

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
        AutoKit.tick();
        InvCleaner.tick();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci) {
        AutoMine.tick();
        if (ModConfig.HANDLER.instance().autoMine.enabled && AutoMine.shouldMine() && (screen != null || overlay != null)) {
            missTime = 0;
            continueAttack(true);
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    public void onHandleBlockBreaking(boolean breaking, CallbackInfo ci) {
        if (breaking && ToolSaver.shouldCancelAttack()) {
            ci.cancel();
        }
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    public void onDoAttack(CallbackInfoReturnable<Boolean> cir) {
        if (ToolSaver.shouldCancelAttack()) {
            cir.cancel();
        }
    }
}
