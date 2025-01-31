package net.xolt.sbutils.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.xolt.sbutils.SbUtils;
import net.xolt.sbutils.feature.features.Notifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "render", at = @At("TAIL"))
    public void render(PoseStack matrices, float partialTick, CallbackInfo ci) {
        SbUtils.FEATURES.get(Notifier.class).onRenderGui(matrices);
    }
}
