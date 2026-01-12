package net.xolt.sbutils.mixins;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Gui;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.feature.features.Notifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//? if >=1.21 {
import net.minecraft.client.DeltaTracker;
//? }

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "render", at = @At("TAIL"))
    public void render(
                        GuiGraphics guiGraphics,
                        //? if >=1.21 {
                        DeltaTracker deltaTracker,
                        //? } else
                        //float deltaTracker,
                        CallbackInfo ci) {
        SbUtils.FEATURES.get(Notifier.class).onRenderGui(guiGraphics);
    }
}
