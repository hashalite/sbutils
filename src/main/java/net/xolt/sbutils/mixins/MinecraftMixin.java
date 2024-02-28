package net.xolt.sbutils.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.feature.features.InvCleaner;
import net.xolt.sbutils.feature.features.*;
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

    @Shadow public int missTime;

    @Shadow
    protected abstract void continueAttack(boolean breaking);

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        SbUtils.FEATURES.get(EnchantAll.class).tick();
        SbUtils.FEATURES.get(AutoAdvert.class).tick();
        SbUtils.FEATURES.get(JoinCommands.class).tick();
        SbUtils.FEATURES.get(AutoCrate.class).tick();
        SbUtils.FEATURES.get(AutoSilk.class).tick();
        SbUtils.FEATURES.get(AutoFix.class).tick();
        SbUtils.FEATURES.get(AutoRaffle.class).tick();
        SbUtils.FEATURES.get(AutoReply.class).tick();
        SbUtils.FEATURES.get(AutoCommand.class).tick();
        SbUtils.FEATURES.get(AutoKit.class).tick();
        SbUtils.FEATURES.get(InvCleaner.class).tick();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTickTail(CallbackInfo ci) {
        SbUtils.FEATURES.get(AutoMine.class).tick();
        if (ModConfig.HANDLER.instance().autoMine.enabled && AutoMine.shouldMine() && (screen != null || overlay != null)) {
            missTime = 0;
            continueAttack(true);
        }
    }

    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    public void onHandleBlockBreaking(boolean breaking, CallbackInfo ci) {
        SbUtils.FEATURES.get(ToolSaver.class).onContinueAttack(breaking, ci);
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    public void onDoAttack(CallbackInfoReturnable<Boolean> cir) {
        SbUtils.FEATURES.get(ToolSaver.class).onStartAttack(cir);
    }
}
